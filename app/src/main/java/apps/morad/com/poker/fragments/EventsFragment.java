package apps.morad.com.poker.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;
import com.activeandroid.query.Select;
import com.facebook.Profile;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.EventsCursorAdapter;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.interfaces.ITaggedFragment;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.thirdParty.BottomSheetDialog;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 12/11/2015.
 */
public class EventsFragment extends Fragment implements ITaggedFragment{

    public static final String FRAGMENT_TAG = "EventsFragment";

    private static final int LOADER_ID = 235;

    private static EventsFragment _instance;

    private BroadcastReceiver _eventsUpdatedBroadcastReceiver;

    public static EventsFragment getInstance()
    {
        if(_instance == null)
        {
            _instance = new EventsFragment();
        }

        return _instance;
    }

    EventsCursorAdapter adapter;
    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;
    FloatingActionButton fab;

    MemberInEventAdapter acceptedAdapter, rejectedAdapter, noStatusAdapter;
    BottomSheetDialog mBottomSheetDialog;
    View bottomSheetContentView;

    ArrayList<String> acceptedMembers;
    ArrayList<String> rejectedMembers;
    ArrayList<String> noStatusMembers;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.events_fragment, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        final Member member = MembersLoader.getById(Profile.getCurrentProfile().getId());

        fab = (FloatingActionButton) rootView.findViewById(R.id.add_event_btn);

        if(member == null || !member.getIsAdmin()){
            fab.setVisibility(View.GONE);
        }

        final Event event = new Select().from(Event.class).where(Event.IS_CLOSED_COLUMN + "= 0").executeSingle();

        if(event != null){
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag(AddOrUpdateEventDialogFragment.TAG);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);

                // Create and show the dialog.
                DialogFragment newFragment = AddOrUpdateEventDialogFragment.newInstance();
                newFragment.setCancelable(false);
                newFragment.show(ft, AddOrUpdateEventDialogFragment.TAG);
            }
        });

        ListView lst = ((ListView)rootView.findViewById(R.id.list_of_events));

        lst.setDivider(null);
        adapter = new EventsCursorAdapter(getActivity(), null);
        lst.setAdapter(adapter);

        mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.Material_App_BottomSheetDialog);
        bottomSheetContentView =  LayoutInflater.from(getActivity()).inflate(R.layout.event_member_layout, null);

        acceptedMembers = new ArrayList<String>();
        rejectedMembers = new ArrayList<String>();
        noStatusMembers = new ArrayList<String>();

        acceptedAdapter = new MemberInEventAdapter(getActivity(), acceptedMembers);
        rejectedAdapter = new MemberInEventAdapter(getActivity(), rejectedMembers);
        noStatusAdapter = new MemberInEventAdapter(getActivity(), noStatusMembers);

        final GridView acceptedGrid = ((GridView) bottomSheetContentView.findViewById(R.id.accepted_members));
        final GridView rejectedGrid = ((GridView) bottomSheetContentView.findViewById(R.id.rejected_members));
        final GridView noStatusGrid = (GridView) bottomSheetContentView.findViewById(R.id.no_satus_members);

        acceptedGrid.setAdapter(acceptedAdapter);
        rejectedGrid.setAdapter(rejectedAdapter);
        noStatusGrid.setAdapter(noStatusAdapter);

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                final String eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID_COLUMN));
                final boolean isClosed = (cursor.getInt(cursor.getColumnIndex(Event.IS_CLOSED_COLUMN)) == 1);
                ShowBottomSheetDialog(eventId, isClosed, member, acceptedGrid, rejectedGrid, noStatusGrid);
            }
        });
    }

    private void ShowBottomSheetDialog(final String eventId, final boolean isClosed, final Member member,GridView acceptedGrid, GridView rejectedGrid, GridView noStatusGrid ) {


        View noAcceptedMsg = bottomSheetContentView.findViewById(R.id.no_accepted_message);
        View noRejectedMsg = bottomSheetContentView.findViewById(R.id.no_rejected_message);
        View noStatusMembersTitle = bottomSheetContentView.findViewById(R.id.no_satus_members_title);

        mBottomSheetDialog.contentView(bottomSheetContentView).show();

        buildMembersStatuses(eventId, isClosed);

        if (acceptedMembers.size() > 0) {
            noAcceptedMsg.setVisibility(View.GONE);
            acceptedGrid.setVisibility(View.VISIBLE);
            if (!isClosed && member != null && member.getIsAdmin()) {
                acceptedGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        onAcceptedMemberClicked(parent, position, eventId, isClosed);
                    }
                });
            }
        } else {
            noAcceptedMsg.setVisibility(View.VISIBLE);
            acceptedGrid.setVisibility(View.GONE);
        }


        if (rejectedMembers.size() > 0) {
            noRejectedMsg.setVisibility(View.GONE);
            rejectedGrid.setVisibility(View.VISIBLE);
            if (!isClosed && member != null && member.getIsAdmin()) {
                rejectedGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        onRejectedMemberClicked(parent, position, eventId, isClosed);
                    }
                });
            }

        } else {
            noRejectedMsg.setVisibility(View.VISIBLE);
            rejectedGrid.setVisibility(View.GONE);
        }

        if (noStatusMembers.size() > 0 && !isClosed) {
            noStatusMembersTitle.setVisibility(View.VISIBLE);
            noStatusGrid.setVisibility(View.VISIBLE);

            noStatusGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onNoStatusMemberClicked(parent, position, member, eventId, isClosed);
                }
            });

        } else {
            noStatusMembersTitle.setVisibility(View.GONE);
            noStatusGrid.setVisibility(View.GONE);
        }
    }

    private void onNoStatusMemberClicked(AdapterView<?> parent, int position, Member member, final String eventId, final boolean isClosed) {
        final String memberId = parent.getAdapter().getItem(position).toString();
        Member clickedMember = MembersLoader.getById(memberId);

        if (clickedMember != null && member != null && member.getIsAdmin()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Set status for " + clickedMember.getFirstName())
                    .setCancelable(false)
                    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new MemberInEventStatusTask(eventId, isClosed, memberId).execute(MemberInEvent.ACCEPT_STATUS);
                        }
                    })
                    .setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new MemberInEventStatusTask(eventId, isClosed, memberId).execute(MemberInEvent.REJECT_STATUS);
                        }
                    })
                    .setNeutralButton("Cancel", null);

            builder.create().show();
        }
    }

    private void onRejectedMemberClicked(AdapterView<?> parent, int position, final String eventId, final boolean isClosed) {
        final String memberId = parent.getAdapter().getItem(position).toString();
        Member member = MembersLoader.getById(memberId);

        if (member != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Change status of " + member.getFirstName() + " to Accept !")
                    .setCancelable(false)
                    .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new MemberInEventStatusTask(eventId, isClosed, memberId).execute(MemberInEvent.ACCEPT_STATUS);
                        }
                    })
                    .setNegativeButton("Cancel", null);

            builder.create().show();
        }
    }

    private void onAcceptedMemberClicked(AdapterView<?> parent, int position, final String eventId, final boolean isClosed) {
        final String memberId = parent.getAdapter().getItem(position).toString();
        Member member = MembersLoader.getById(memberId);

        if (member != null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setMessage("Change status of " + member.getFirstName() + " to Reject !")
                    .setCancelable(false)
                    .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new MemberInEventStatusTask(eventId, isClosed, memberId).execute(MemberInEvent.REJECT_STATUS);
                        }
                    })
                    .setNegativeButton("Cancel", null);

            builder.create().show();

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(Event.class, null),
                        null, null, null, Event.CREATED_DATE_COLUMN + " DESC");
            }

            @Override
            public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                adapter.swapCursor(data);
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                adapter.swapCursor(null);
            }
        };

        getActivity().getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);

        _eventsUpdatedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Event event = new Select().from(Event.class).where(Event.IS_CLOSED_COLUMN + "= 0").executeSingle();

                if(event != null){
                    fab.setVisibility(View.GONE);
                }
                else {
                    Member member = MembersLoader.getById(Profile.getCurrentProfile().getId());

                    if(member != null && member.getIsAdmin()){
                        fab.setVisibility(View.VISIBLE);
                    }
                }

                getActivity().getLoaderManager().restartLoader(LOADER_ID, null, loaderCallbacks);
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.EVENTS_UPDATED);
        intentFilter.addAction(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_eventsUpdatedBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_eventsUpdatedBroadcastReceiver);
    }

    private void buildMembersStatuses(String eventId, boolean isClosed){

        acceptedMembers.clear();
        rejectedMembers.clear();
        noStatusMembers.clear();

        List<MemberInEvent> membersInEvents = new Select().from(MemberInEvent.class)
                .where(MemberInEvent.EVENT_ID_COLUMN + "= ?", eventId).execute();

        for(MemberInEvent mIE : membersInEvents){

            if(mIE.getStatus() == MemberInEvent.ACCEPT_STATUS){
                acceptedMembers.add(mIE.getFbId());
            }else if(mIE.getStatus() == MemberInEvent.REJECT_STATUS){
                rejectedMembers.add(mIE.getFbId());
            }
        }

        List<Member> members = MembersLoader.getAll();
        if(!isClosed) {
            for (Member m : members) {
                noStatusMembers.add(m.getFbId());
            }
            noStatusMembers.removeAll(acceptedMembers);
            noStatusMembers.removeAll(rejectedMembers);
        }
    }

    public class MemberInEventStatusTask extends AsyncTask<Integer,Void,Void> {

        String eventId;
        String memberId;
        boolean isClosed;
        public MemberInEventStatusTask(String eventId, boolean isClosed, String memberId){

            this.eventId = eventId;
            this.memberId = memberId;
            this.isClosed = isClosed;
        }

        SharedPreferences _pref;
        String _url;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            _url = _pref.getString(getActivity().getString(R.string.pref_server_url), "http://localhost");
        }
        @Override
        protected Void doInBackground(Integer... params) {

            // send to server
            MemberInEvent memberInEvent = new MemberInEvent(memberId, eventId, params[0]);
            try {
                // send to the server
                Utilities.sendRequest(_url + "/memberInEventChangeStatus", "POST",
                        new JSONObject(Utilities.mapper.writeValueAsString(memberInEvent)));
            }
            catch (Exception e){
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            buildMembersStatuses(eventId, isClosed);

            rejectedAdapter.swapProfiles(rejectedMembers);
            acceptedAdapter.swapProfiles(acceptedMembers);
            if(!isClosed)
                noStatusAdapter.swapProfiles(noStatusMembers);
        }
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }
}

package apps.morad.com.poker.fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

public class MembersInEventFragment extends Fragment {

    private BroadcastReceiver mBroadcastReceiver;

    MemberInEventAdapter acceptedAdapter, rejectedAdapter, noStatusAdapter;
    ArrayList<String> acceptedMembers;
    ArrayList<String> rejectedMembers;
    ArrayList<String> noStatusMembers;
    GridView acceptedGrid, rejectedGrid, noStatusGrid;
    private Event mEvent;

    public MembersInEventFragment() {
    }

    public static MembersInEventFragment newInstance(String eventId) {
        MembersInEventFragment fragment = new MembersInEventFragment();
        Bundle args = new Bundle();
        args.putString(Event.EVENT_ID_COLUMN, eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                buildMembersStatuses(mEvent.getEventId(), mEvent.isClosed());

                rejectedAdapter.swapProfiles(rejectedMembers);
                acceptedAdapter.swapProfiles(acceptedMembers);
                if(!mEvent.isClosed())
                    noStatusAdapter.swapProfiles(noStatusMembers);
            }
        };

        if (getArguments() != null) {
            String eventId = getArguments().getString(Event.EVENT_ID_COLUMN);
            mEvent = new Select().from(Event.class).where(Event.EVENT_ID_COLUMN + "=?", eventId).executeSingle();

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView =  inflater.inflate(R.layout.fragment_members_in_event, container, false);
        acceptedMembers = new ArrayList<String>();
        rejectedMembers = new ArrayList<String>();
        noStatusMembers = new ArrayList<String>();
        acceptedAdapter = new MemberInEventAdapter(getActivity(), acceptedMembers);
        rejectedAdapter = new MemberInEventAdapter(getActivity(), rejectedMembers);
        noStatusAdapter = new MemberInEventAdapter(getActivity(), noStatusMembers);
        acceptedGrid = (GridView) rootView.findViewById(R.id.accepted_members);
        rejectedGrid = (GridView) rootView.findViewById(R.id.rejected_members);
        noStatusGrid = (GridView) rootView.findViewById(R.id.no_satus_members);
        acceptedGrid.setAdapter(acceptedAdapter);
        rejectedGrid.setAdapter(rejectedAdapter);
        noStatusGrid.setAdapter(noStatusAdapter);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        View noAcceptedMsg = view.findViewById(R.id.no_accepted_message);
        View noRejectedMsg = view.findViewById(R.id.no_rejected_message);
        View noStatusMembersTitle = view.findViewById(R.id.no_satus_members_title);

        final String eventId = mEvent != null ? mEvent.getEventId() : "0";
        final boolean isClosed = mEvent !=null && mEvent.isClosed();
        buildMembersStatuses(eventId, isClosed);
        final Member member = MembersLoader.getById(Profile.getCurrentProfile().getId());
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

        rejectedAdapter.swapProfiles(rejectedMembers);
        acceptedAdapter.swapProfiles(acceptedMembers);
        if(!mEvent.isClosed())
            noStatusAdapter.swapProfiles(noStatusMembers);
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

    public class MemberInEventStatusTask extends AsyncTask<Integer, Void, Void> {

        String eventId;
        String memberId;
        boolean isClosed;
        public MemberInEventStatusTask(String eventId, boolean isClosed, String memberId){

            this.eventId = eventId;
            this.memberId = memberId;
            this.isClosed = isClosed;
        }

        String _url;
        @Override
        protected void onPreExecute() {
            _url = getActivity().getResources().getString(R.string.server_url);
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.EVENTS_UPDATED);
        intentFilter.addAction(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }
}

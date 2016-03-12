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
import apps.morad.com.poker.activities.EventDetailsActivity;
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

        lst.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getAdapter().getItem(position);
                final String eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID_COLUMN));
//                final boolean isClosed = (cursor.getInt(cursor.getColumnIndex(Event.IS_CLOSED_COLUMN)) == 1);
//                ShowBottomSheetDialog(eventId, isClosed, member, acceptedGrid, rejectedGrid, noStatusGrid);

                Intent intent = new Intent(getActivity(), EventDetailsActivity.class);
                intent.putExtra(Event.EVENT_ID_COLUMN, eventId);
                startActivity(intent);
            }
        });
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

        getActivity().getLoaderManager().restartLoader(LOADER_ID, null, loaderCallbacks);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_eventsUpdatedBroadcastReceiver);
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }
}

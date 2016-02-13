package apps.morad.com.poker.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.MembersCursorAdapter;
import apps.morad.com.poker.interfaces.ITaggedFragment;
import apps.morad.com.poker.models.Member;

/**
 * Created by Morad on 12/13/2015.
 */
public class MembersFragment extends Fragment implements ITaggedFragment {
    public static final String FRAGMENT_TAG = "MembersFragment";

    private static MembersFragment _instance;

    public static MembersFragment getInstance()
    {
        if(_instance == null)
        {
            _instance = new MembersFragment();
        }

        return _instance;
    }

    private  static  final int LOADER_ID = 123;

    MembersCursorAdapter adapter;

    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;

    private BroadcastReceiver _membersUpdatedBroadcastReceiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.members_fragment, container, false);

        ListView lst = ((ListView)rootView.findViewById(R.id.list_of_members));

        lst.setDivider(null);

        adapter = new MembersCursorAdapter(getActivity(), null);

        lst.setAdapter(adapter);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                return new CursorLoader(getActivity(),
                        ContentProvider.createUri(Member.class, null),
                        null, null, null, Member.SCORE_COLUMN + " DESC");
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

        _membersUpdatedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LoaderManager loaderManager = getActivity().getLoaderManager();
                if(loaderManager != null){
                    loaderManager.restartLoader(LOADER_ID, null, loaderCallbacks);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_membersUpdatedBroadcastReceiver, new IntentFilter(MainActivity.MEMBERS_UPDATED));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_membersUpdatedBroadcastReceiver);
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }
}

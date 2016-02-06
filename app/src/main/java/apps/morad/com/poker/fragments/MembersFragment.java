package apps.morad.com.poker.fragments;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.activeandroid.content.ContentProvider;

import java.util.Objects;

import apps.morad.com.poker.R;
import apps.morad.com.poker.adapters.MembersCursorAdapter;
import apps.morad.com.poker.interfaces.IRefreshView;
import apps.morad.com.poker.interfaces.ITaggedFragment;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 12/13/2015.
 */
public class MembersFragment extends Fragment implements IRefreshView, ITaggedFragment {
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

    boolean isCompleteLoading = false;

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
                isCompleteLoading = true;
            }

            @Override
            public void onLoaderReset(Loader<Cursor> loader) {
                adapter.swapCursor(null);
            }
        };

        getActivity().getLoaderManager().initLoader(LOADER_ID, null, loaderCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();
        //isCompleteLoading = false;
    }

    @Override
    public void refreshView() {

        if(isCompleteLoading){
            isCompleteLoading = false;
            getActivity().getLoaderManager().restartLoader(LOADER_ID, null, loaderCallbacks);
        }
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }
}

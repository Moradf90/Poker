package apps.morad.com.poker.fragments;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.activeandroid.query.Select;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.GamesCursorAdapter;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.utilities.Utilities;

public class GamesInEventFragment extends Fragment implements AdapterView.OnItemClickListener{

    private BroadcastReceiver mBroadcastReceiver;
    private Event mEvent;
    private GamesCursorAdapter mGamesAdapter;

    public GamesInEventFragment() {
        // Required empty public constructor
    }

    public static GamesInEventFragment newInstance(String eventId) {
        GamesInEventFragment fragment = new GamesInEventFragment();
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

                // refresh the games
                if(mEvent != null) {
                    mGamesAdapter.swapCursor(Utilities.fetchResultCursor(Game.class, true,
                            Game.ORDER_ID_COLUMN, Game.EVENT_ID_COLUMN + "=?", mEvent.getEventId()));
                    mGamesAdapter.notifyDataSetChanged();
                }
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_games_in_event, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGamesAdapter = new GamesCursorAdapter(getActivity(), Utilities.fetchResultCursor(Game.class, true,
                Game.ORDER_ID_COLUMN, Game.EVENT_ID_COLUMN + "=?", mEvent.getEventId()));
        ListView lst = (ListView) view.findViewById(R.id.list_of_games);
        lst.setDivider(null);
        lst.setAdapter(mGamesAdapter);
        lst.setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.GAMES_UPDATED);
        intentFilter.addAction(MainActivity.MEMBERS_IN_GAMES_UPDATED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor)mGamesAdapter.getItem(position);
        String gameId = cursor.getString(cursor.getColumnIndex(Game.GAME_ID_COLUMN));

        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        android.app.Fragment prev = getActivity().getFragmentManager().findFragmentByTag("GameDetails");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        GameDetailsDialogFragment newFragment = GameDetailsDialogFragment.newInstance(gameId);
        newFragment.show(ft, "GameDetails");
    }
}

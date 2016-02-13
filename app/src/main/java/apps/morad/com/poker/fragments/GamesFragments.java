package apps.morad.com.poker.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
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
import android.support.design.widget.Snackbar;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.AddOrUpdateGameActivity;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.EventGameGroupCursorAdapter;
import apps.morad.com.poker.adapters.GamesCursorAdapter;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.adapters.MemberOutGameAdapter;
import apps.morad.com.poker.customUI.LinearViewAdapter;
import apps.morad.com.poker.interfaces.ITaggedFragment;
import apps.morad.com.poker.interfaces.OnDragStateChangeListener;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.thirdParty.BottomSheetDialog;
import apps.morad.com.poker.thirdParty.widget.InboxBackgroundScrollView;
import apps.morad.com.poker.thirdParty.widget.InboxLayoutBase;
import apps.morad.com.poker.thirdParty.widget.InboxLayoutListView;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 12/25/2015.
 */
public class GamesFragments extends Fragment implements ITaggedFragment{

    public static final String FRAGMENT_TAG = "GamesFragment";
    private static final int LOADER_ID = 365;

    private static GamesFragments _instance;

    public static GamesFragments getInstance()
    {
        if(_instance == null)
        {
            _instance = new GamesFragments();
        }

        return _instance;
    }

    private BroadcastReceiver _gamesUpdatedBroadcastReceiver;

    InboxLayoutListView inboxLayoutListView;
    GamesCursorAdapter gamesAdapter;
    LoaderManager.LoaderCallbacks<Cursor> loaderCallbacks;
    EventGameGroupCursorAdapter adapter;
    FloatingActionButton fab;
    String eventId = "";

    MemberOutGameAdapter memberOutGameAdapter;
    MemberInEventAdapter memberInGameAdapter;


    BottomSheetDialog mBottomSheetDialog;
    View bottomSheetContentView;

    ArrayList<String> membersInGame;
    ArrayList<MemberInGame> membersOutGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.games_fragments, container, false);

        Member member = MembersLoader.getById(Profile.getCurrentProfile().getId());
        fab = (FloatingActionButton) rootView.findViewById(R.id.add_game_btn);

        if(member == null || !member.getIsAdmin()){
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if no opened event exist
                onAddGameButtonPressed(rootView);
            }
        });

        final InboxBackgroundScrollView scrollLayout = (InboxBackgroundScrollView) rootView.findViewById(R.id.scroll);
        scrollLayout.setShadowColor(getResources().getColor(R.color.colorPrimaryDark));
        inboxLayoutListView = (InboxLayoutListView)rootView.findViewById(R.id.list_of_games);
        inboxLayoutListView.setBackgroundScrollView(scrollLayout);
        inboxLayoutListView.setOnDragStateChangeListener(new OnDragStateChangeListener() {
            @Override
            public void dragStateChange(InboxLayoutBase.DragState state) {
                switch (state) {
                    case CANCLOSE:
                        eventId = "";
                        break;
                    case CANNOTCLOSE:
                        break;
                }
            }
        });

        gamesAdapter = new GamesCursorAdapter(getActivity(),Utilities.fetchResultCursor(Game.class));
        inboxLayoutListView.getDragableView().setDivider(null);
        inboxLayoutListView.setAdapter(gamesAdapter);

        mBottomSheetDialog = new BottomSheetDialog(getActivity(), R.style.Material_App_BottomSheetDialog);
        bottomSheetContentView = LayoutInflater.from(getActivity()).inflate(R.layout.game_members_layout, null);

        membersInGame = new ArrayList<String>();
        membersOutGame = new ArrayList<MemberInGame>();

        final GridView memberInGameGrid = ((GridView)bottomSheetContentView.findViewById(R.id.members_in));
        final ListView membersOutList = ((ListView)bottomSheetContentView.findViewById(R.id.members_out));
        membersOutList.setDivider(null);

        memberOutGameAdapter = new MemberOutGameAdapter(getActivity(), membersOutGame);
        memberInGameAdapter = new MemberInEventAdapter(getActivity(), membersInGame);

        memberInGameGrid.setAdapter(memberInGameAdapter);
        membersOutList.setAdapter(memberOutGameAdapter);

        inboxLayoutListView.getDragableView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor)inboxLayoutListView.getDragableView().getAdapter().getItem(position);
                final String gameId = cursor.getString(cursor.getColumnIndex(Game.GAME_ID_COLUMN));
                long startTime = cursor.getLong(cursor.getColumnIndex(Game.START_TIME_COLUMN));
                long endTime = cursor.getLong(cursor.getColumnIndex(Game.END_TIME_COLUMN));

                final boolean isStartedGame = startTime != Game.NOT_STARTED_GAME_TIME;
                final boolean isFinishedGame = endTime != Game.NOT_FINISHED_GAME_TIME;

                bottomSheetContentView.findViewById(R.id.divider).setVisibility(View.VISIBLE);
                bottomSheetContentView.findViewById(R.id.out_members_title).setVisibility(View.VISIBLE);
                bottomSheetContentView.findViewById(R.id.members_out).setVisibility(View.VISIBLE);
                bottomSheetContentView.findViewById(R.id.in_members_title).setVisibility(View.VISIBLE);
                bottomSheetContentView.findViewById(R.id.out_members_title).setVisibility(View.VISIBLE);

                if(!isStartedGame){
                    bottomSheetContentView.findViewById(R.id.divider).setVisibility(View.GONE);
                    bottomSheetContentView.findViewById(R.id.out_members_title).setVisibility(View.GONE);
                    bottomSheetContentView.findViewById(R.id.members_out).setVisibility(View.GONE);
                }

                if(isFinishedGame){
                    bottomSheetContentView.findViewById(R.id.in_members_title).setVisibility(View.GONE);
                    bottomSheetContentView.findViewById(R.id.divider).setVisibility(View.GONE);
                    bottomSheetContentView.findViewById(R.id.out_members_title).setVisibility(View.GONE);
                }

                mBottomSheetDialog.contentView(bottomSheetContentView).show();
                updateMembersArrays(gameId);

                final Member currentMember = MembersLoader.getById(Profile.getCurrentProfile().getId());
                if(currentMember != null && currentMember.getIsAdmin() && !isFinishedGame) {

                    memberInGameGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            onInGameMemberClicked(parent, position, isStartedGame, isFinishedGame, gameId);
                        }
                    });

                    membersOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            onOutMemberClicked(parent, position, isStartedGame, isFinishedGame, gameId);
                        }
                    });
                }
            }
        });

        // list of event
        adapter = new EventGameGroupCursorAdapter(getActivity(),null);

        final LinearViewAdapter lad = (LinearViewAdapter) rootView.findViewById(R.id.events_layouts);

        lad.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = (Cursor) lad.getAdapter().getItem(position);
                eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID_COLUMN));

                gamesAdapter.swapCursor(Utilities.fetchResultCursor(Game.class, false,
                        Game.ORDER_ID_COLUMN, Game.EVENT_ID_COLUMN + "=?", eventId));

                inboxLayoutListView.openWithAnim(view);
            }
        });

        lad.setAdapter(adapter);

        return rootView;
    }

    private void onInGameMemberClicked(AdapterView<?> parent, int position, boolean isStartedGame, boolean isFinishedGame, final String gameId) {
        final String memberId = parent.getAdapter().getItem(position).toString();
        Member member = MembersLoader.getById(memberId);
        if (member != null && isStartedGame && !isFinishedGame) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Out")
                    .setMessage("Is " + member.getFirstName() + " get out ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new MemberOutGameTask(gameId).execute(memberId);
                        }
                    })
                    .setNegativeButton("No", null);

            builder.create().show();
        }
    }

    private void onOutMemberClicked(AdapterView<?> parent, int position, boolean isStartedGame, boolean isFinishedGame, final String gameId) {
        final MemberInGame memberInGame = (MemberInGame) parent.getAdapter().getItem(position);

        final Member member = MembersLoader.getById(memberInGame.getFbId());

        if(member != null && isStartedGame && !isFinishedGame && !memberInGameAdapter.containsProfile(member.getFbId())
                && memberInGameAdapter.getCount() >= Game.NUMBER_OF_MEMBERS_GET_POINTS){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setTitle("Another round")
                    .setMessage("Is " + member.getFirstName() + " takes another round ?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            new AnotherRoundTask(gameId).execute(memberInGame.getFbId());
                        }
                    })
                    .setNegativeButton("No", null);

            builder.create().show();
        }
    }

    private void onAddGameButtonPressed(View rootView) {
        Event event = new Select().from(Event.class).where(Event.IS_CLOSED_COLUMN + "= 0").executeSingle();
        if (event == null) {
            Snackbar snackbar = Snackbar.make(rootView, "no opened event.", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
            snackbar.show();
        } else {

            if (event.getDate() > new Date().getTime()) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String d = sdf.format(new Date(event.getDate()));

                Snackbar snackbar = Snackbar.make(rootView, "event starts at " + d + ", \n you can not create a game before the event starts", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                snackbar.show();
            } else {
                // check if there accepted members to the event

                List<MemberInEvent> mie = new Select().from(MemberInEvent.class)
                        .where(MemberInEvent.EVENT_ID_COLUMN + " = ? and " + MemberInEvent.STATUS_COLUMN + " = ?"
                                , event.getEventId(), MemberInEvent.ACCEPT_STATUS).execute();

                if (mie.size() < Game.MIN_NUMBER_OF_MEMBERS) {
                    Snackbar snackbar = Snackbar.make(rootView, "No enough accepted members.", Snackbar.LENGTH_LONG);
                    snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                    snackbar.show();
                } else {
                    Intent intent = new Intent(getActivity(), AddOrUpdateGameActivity.class);
                    intent.putExtra(Game.EVENT_ID_COLUMN, event.getEventId());
                    startActivity(intent);
                }
            }
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

        _gamesUpdatedBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                    getActivity().getLoaderManager().restartLoader(LOADER_ID, null, loaderCallbacks);


                // refresh the games
                if(!eventId.isEmpty()) {
                    gamesAdapter.swapCursor(Utilities.fetchResultCursor(Game.class, true,
                            Game.ORDER_ID_COLUMN, Game.EVENT_ID_COLUMN + "=?", eventId));
                    gamesAdapter.notifyDataSetChanged();
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.EVENTS_UPDATED);
        intentFilter.addAction(MainActivity.GAMES_UPDATED);
        intentFilter.addAction(MainActivity.MEMBERS_IN_GAMES_UPDATED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(_gamesUpdatedBroadcastReceiver,
                intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(_gamesUpdatedBroadcastReceiver);
    }

    private void updateMembersArrays(String gameId){

        membersInGame.clear();
        membersOutGame.clear();

        List<MemberInGame> members = new Select().from(MemberInGame.class)
                .where(MemberInGame.GAME_ID_COLUMN + "=?", gameId).execute();

        for (MemberInGame mig : members) {
            if (mig.getOrder() == -1){
                membersInGame.add(mig.getFbId());
            }
            else {
                membersOutGame.add(mig);
            }
        }

        Collections.sort(membersOutGame, new Comparator<MemberInGame>() {
            @Override
            public int compare(MemberInGame lhs, MemberInGame rhs) {
                return rhs.getOrder() - lhs.getOrder();
            }
        });

        memberInGameAdapter.swapProfiles(membersInGame);
        memberOutGameAdapter.swapProfiles(membersOutGame);
    }

    public class MemberOutGameTask extends AsyncTask<String,Void,Void> {

        String gameId;

        public MemberOutGameTask(String gameId){
            this.gameId = gameId;
        }

        String url;
        @Override
        protected void onPreExecute() {
            SharedPreferences _pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            url = _pref.getString(getActivity().getString(R.string.pref_server_url), "http://localhost");
        }
        @Override
        protected Void doInBackground(String... params) {

            if(params.length > 0){

                String memberId = params[0];

                List<MemberInGame> members = new Select().from(MemberInGame.class)
                        .where(MemberInGame.GAME_ID_COLUMN + "=?", this.gameId).execute();

                long migId = -1;
                int order = 1;
                for (MemberInGame mig : members) {
                    if (mig.getOrder() != -1){
                        order++;
                    }
                    else if(mig.getFbId().equals(memberId)){
                        migId = mig.getId();
                    }
                }

                int score = Game.getScoreByOrder(members.size(), order);

                MemberInGame mig = MemberInGame.load(MemberInGame.class, migId).setScore(score).setOrder(order);

                try {
                    Utilities.sendRequest(url + "/memberInGameUpdate", "POST",
                            new JSONObject(Utilities.mapper.writeValueAsString(mig)));
                }
                catch (Exception e) {}

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateMembersArrays(gameId);
        }
    }

    public class AnotherRoundTask extends AsyncTask<String,Void,Void>{

        String gameId;

        public AnotherRoundTask(String gameId){
            this.gameId = gameId;
        }

        String url;
        @Override
        protected void onPreExecute() {
            SharedPreferences _pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            url = _pref.getString(getActivity().getString(R.string.pref_server_url), "http://localhost");
        }
        @Override
        protected Void doInBackground(String... params) {

            if(params.length > 0){

                String memberId = params[0];

                List<MemberInGame> migs = new Select().from(MemberInGame.class)
                        .where(MemberInGame.GAME_ID_COLUMN + "=? and " + MemberInGame.FB_ID_COLUMN + "= ?",
                                gameId, memberId).execute();

                MemberInGame mig = new MemberInGame(memberId, gameId);
                mig.setRound(migs.size() + 1);

                try {
                    Utilities.sendRequest(url + "/memberInGameUpdate", "POST",
                            new JSONObject(Utilities.mapper.writeValueAsString(mig)));
                }
                catch (Exception e) {}
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateMembersArrays(gameId);
        }
    }

    @Override
    public String getFragmentTag() {
        return FRAGMENT_TAG;
    }
}

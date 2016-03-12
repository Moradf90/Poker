package apps.morad.com.poker.fragments;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.adapters.MemberOutGameAdapter;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 3/5/2016.
 */
public class GameDetailsDialogFragment extends DialogFragment {

    public static GameDetailsDialogFragment newInstance(String gameId){
        GameDetailsDialogFragment fragment = new GameDetailsDialogFragment();
        Bundle args = new Bundle();
        args.putString(Game.GAME_ID_COLUMN, gameId);
        fragment.setArguments(args);
        return fragment;
    }

    private Game mGame;
    MemberOutGameAdapter memberOutGameAdapter;
    MemberInEventAdapter memberInGameAdapter;
    ArrayList<String> membersInGame;
    ArrayList<MemberInGame> membersOutGame;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                updateMembersArrays();
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.game_members_layout, container, false);

        if(getArguments() != null)
        {
            mGame = new Select().from(Game.class)
                    .where(Game.GAME_ID_COLUMN + "=?", getArguments().getString(Game.GAME_ID_COLUMN))
                    .executeSingle();
        }

        getDialog().setTitle(mGame != null ? mGame.getName() : "");

        membersInGame = new ArrayList<String>();
        membersOutGame = new ArrayList<MemberInGame>();
        GridView memberInGameGrid = ((GridView)v.findViewById(R.id.members_in));
        ListView membersOutList = ((ListView)v.findViewById(R.id.members_out));
        membersOutList.setDivider(null);
        memberOutGameAdapter = new MemberOutGameAdapter(getActivity(), membersOutGame);
        memberInGameAdapter = new MemberInEventAdapter(getActivity(), membersInGame);

        memberInGameGrid.setAdapter(memberInGameAdapter);
        membersOutList.setAdapter(memberOutGameAdapter);


        final boolean isStartedGame = mGame.getStartTime() != Game.NOT_STARTED_GAME_TIME;
        final boolean isFinishedGame = mGame.getEndTime() != Game.NOT_FINISHED_GAME_TIME;

        v.findViewById(R.id.divider).setVisibility(View.VISIBLE);
        v.findViewById(R.id.out_members_title).setVisibility(View.VISIBLE);
        v.findViewById(R.id.members_out).setVisibility(View.VISIBLE);
        v.findViewById(R.id.in_members_title).setVisibility(View.VISIBLE);
        v.findViewById(R.id.out_members_title).setVisibility(View.VISIBLE);

        if(!isStartedGame){
            v.findViewById(R.id.divider).setVisibility(View.GONE);
            v.findViewById(R.id.out_members_title).setVisibility(View.GONE);
            v.findViewById(R.id.members_out).setVisibility(View.GONE);
        }

        if(isFinishedGame){
            v.findViewById(R.id.in_members_title).setVisibility(View.GONE);
            v.findViewById(R.id.divider).setVisibility(View.GONE);
            v.findViewById(R.id.out_members_title).setVisibility(View.GONE);
        }

        updateMembersArrays();

        final Member currentMember = MembersLoader.getById(Profile.getCurrentProfile().getId());
        if(currentMember != null && currentMember.getIsAdmin() && !isFinishedGame) {

            memberInGameGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onInGameMemberClicked(parent, position, isStartedGame, isFinishedGame);
                }
            });

            membersOutList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onOutMemberClicked(parent, position, isStartedGame, isFinishedGame);
                }
            });
        }

        return v;
    }

    private void updateMembersArrays(){

        membersInGame.clear();
        membersOutGame.clear();

        List<MemberInGame> members = new Select().from(MemberInGame.class)
                .where(MemberInGame.GAME_ID_COLUMN + "=?", mGame.getGameId()).execute();

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

    private void onInGameMemberClicked(AdapterView<?> parent, int position, boolean isStartedGame, boolean isFinishedGame) {
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
                            new MemberOutGameTask(mGame.getGameId()).execute(memberId);
                        }
                    })
                    .setNegativeButton("No", null);

            builder.create().show();
        }
    }

    private void onOutMemberClicked(AdapterView<?> parent, int position, boolean isStartedGame, boolean isFinishedGame) {
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
                            new AnotherRoundTask(mGame.getGameId()).execute(memberInGame.getFbId());
                        }
                    })
                    .setNegativeButton("No", null);

            builder.create().show();
        }
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
}

package apps.morad.com.poker.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.facebook.Profile;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 12/31/2015.
 */
 public class GamesCursorAdapter extends CursorAdapter {

    private static class ViewHolder{
        TextView name;
        TextView order;
        View actions;
        TextView startTime;
        TextView endTime;
        View notStartedLayout;
        View notFinishedLayout;
        View deleteAction;
        View startBtn;
        View finishBtn;
    }

    Context _context;
    public GamesCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
        _context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.game_in_list, parent, false);

        ViewHolder holder = new ViewHolder();
        holder.name = (TextView)view.findViewById(R.id.game_name);
        holder.order = (TextView)view.findViewById(R.id.game_order);
        holder.actions = view.findViewById(R.id.actions);
        holder.startTime = (TextView)view.findViewById(R.id.game_start_time);
        holder.endTime = (TextView)view.findViewById(R.id.game_end_time);
        holder.notStartedLayout = view.findViewById(R.id.not_started_game);
        holder.notFinishedLayout = view.findViewById(R.id.not_finished_game);
        holder.deleteAction = view.findViewById(R.id.delete_action);
        holder.startBtn = view.findViewById(R.id.game_start_btn);
        holder.finishBtn = view.findViewById(R.id.game_end_btn);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //Game.printHello();
        final String gameId = cursor.getString(cursor.getColumnIndex(Game.GAME_ID_COLUMN));
        String name = cursor.getString(cursor.getColumnIndex(Game.GAME_NAME_COLUMN));
        long startTime = cursor.getLong(cursor.getColumnIndex(Game.START_TIME_COLUMN));
        long endTime = cursor.getLong(cursor.getColumnIndex(Game.END_TIME_COLUMN));
        boolean isConsidered = (cursor.getInt(cursor.getColumnIndex(Game.IS_CONSIDERED_COLUMN)) == 1);
        int order = cursor.getInt(cursor.getColumnIndex(Game.ORDER_ID_COLUMN));

        ViewHolder holder = (ViewHolder) view.getTag();

        String currentProfileId = Profile.getCurrentProfile().getId();
        Member currentMember = MembersLoader.getById(currentProfileId) ;
        boolean currentIsAdmin = (currentMember != null && currentMember.getIsAdmin());

        holder.name.setText(name);
        holder.order.setText("#" + order);
        holder.actions.setVisibility((currentIsAdmin && endTime == Game.NOT_FINISHED_GAME_TIME) ? View.VISIBLE : View.GONE);
        holder.deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setTitle("Delete Game")
                        .setMessage("Are you sure you want to delete ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                new GameActionTask().execute(gameId, "delete");

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Snackbar snackbar = Snackbar.make(v, "the game reserved.", Snackbar.LENGTH_LONG);
                                snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                                snackbar.show();
                            }
                        });

                builder.create().show();

            }
        });

        holder.notStartedLayout.setVisibility(
                (currentIsAdmin && startTime == Game.NOT_STARTED_GAME_TIME) ? View.VISIBLE : View.GONE);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        holder.startTime.setText(
                (startTime == Game.NOT_STARTED_GAME_TIME) ? "not started." : "starts at " + sdf.format(new Date(startTime))
        );

        holder.notFinishedLayout.setVisibility(
                (currentIsAdmin && startTime != Game.NOT_STARTED_GAME_TIME && endTime == Game.NOT_FINISHED_GAME_TIME) ? View.VISIBLE : View.GONE);

        holder.endTime.setText(
                (startTime == Game.NOT_STARTED_GAME_TIME || endTime == Game.NOT_FINISHED_GAME_TIME) ? "not finished." : "finished at " + sdf.format(new Date(endTime))
        );

        view.findViewById(R.id.game_winners).setVisibility(
                ((startTime != Game.NOT_STARTED_GAME_TIME) && (endTime != Game.NOT_FINISHED_GAME_TIME)) ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.game_winners_title).setVisibility(
                ((startTime != Game.NOT_STARTED_GAME_TIME) && (endTime != Game.NOT_FINISHED_GAME_TIME)) ? View.VISIBLE : View.GONE);

        if((startTime != Game.NOT_STARTED_GAME_TIME) && (endTime != Game.NOT_FINISHED_GAME_TIME)){

            List<MemberInGame> memberInGames = new Select().from(MemberInGame.class)
                    .where(MemberInGame.GAME_ID_COLUMN + "= ?", gameId).execute();

            Collections.sort(memberInGames, new Comparator<MemberInGame>() {
                @Override
                public int compare(MemberInGame lhs, MemberInGame rhs) {
                    return rhs.getOrder() - lhs.getOrder();
                }
            });

            // first Winner
            MemberInGame first = memberInGames.get(0);
            Member member = MembersLoader.getById(first.getFbId());
            View v = view.findViewById(R.id.winner_1);
            ((ProfilePictureView)v.findViewById(R.id.profile_picture)).setProfileId(first.getFbId());
            ((TextView)v.findViewById(R.id.profile_name)).setText(member.getFirstName());

            // second Winner
            MemberInGame second = memberInGames.get(1);
            Member member2 = MembersLoader.getById(second.getFbId());
            View v2 = view.findViewById(R.id.winner_2);
            ((ProfilePictureView)v2.findViewById(R.id.profile_picture)).setProfileId(second.getFbId());
            ((TextView)v2.findViewById(R.id.profile_name)).setText(member2.getFirstName());

            View v3 = view.findViewById(R.id.winner_3);
            view.findViewById(R.id.winner_3_label).setVisibility(View.VISIBLE);
            // third Winner
            if(memberInGames.size() >= Game.NUMBER_OF_MEMBERS_GET_POINTS) {
                MemberInGame third = memberInGames.get(2);
                Member member3 = MembersLoader.getById(third.getFbId());
                ((ProfilePictureView) v3.findViewById(R.id.profile_picture)).setProfileId(third.getFbId());
                ((TextView) v3.findViewById(R.id.profile_name)).setText(member3.getFirstName());
            }
            else {
                v3.findViewById(R.id.profile_picture).setVisibility(View.GONE);
                v3.findViewById(R.id.profile_name).setVisibility(View.GONE);
                view.findViewById(R.id.winner_3_label).setVisibility(View.GONE);
            }
        }

        holder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GameActionTask().execute(gameId, "start");
            }
        });

        holder.finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<MemberInGame> lst = new Select().from(MemberInGame.class)
                        .where(MemberInGame.GAME_ID_COLUMN + "= ? and " + MemberInGame.ORDER_ID_COLUMN + " = ?"
                                , gameId, MemberInGame.NOT_ORDERED_MEMBER).execute();
                if (lst.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context)
                            .setMessage("Please touch the game and order the players.")
                            .setPositiveButton("OK", null);

                    builder.create().show();
                } else {
                    new GameActionTask().execute(gameId, "end");
                }
            }
        });
    }

    public class GameActionTask extends AsyncTask<String,Void,Void> {

        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(_context);
        }
        @Override
        protected Void doInBackground(String... params) {
            // first param is game id
            // second param is the action
            if(params.length >= 2){

                String gameId = params[0];
                String action = params[1];
                String url = _pref.getString(_context.getString(R.string.pref_server_url), "http://localhost");
                try {
                switch (action){
                    case "delete" :
                        Utilities.sendRequest(url + "/deleteGame", "DELETE", new JSONObject().put("gameid", gameId));
                        break;
                    case "start" :
                        Utilities.sendRequest(url + "/updateGame", "POST", new JSONObject().put("gameid", gameId)
                                .put(Game.START_TIME_COLUMN, System.currentTimeMillis()));
                        break;

                    case "end" :
                        Utilities.sendRequest(url + "/updateGame", "POST", new JSONObject().put("gameid", gameId)
                                .put(Game.END_TIME_COLUMN, System.currentTimeMillis()));
                        break;
                }
                }
                catch (Exception e){
                }

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            notifyDataSetChanged();
        }
    }
}

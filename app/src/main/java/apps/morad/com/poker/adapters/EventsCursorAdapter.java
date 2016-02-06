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
import android.widget.ImageView;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.facebook.Profile;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.thirdParty.CircleButton;
import apps.morad.com.poker.utilities.MembersLoader;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 12/16/2015.
 */
public class EventsCursorAdapter extends CursorAdapter {

    private static class ViewHolder{
        TextView title;
        TextView location;
        TextView date;
        TextView creator;
        TextView acceptTag;
        TextView rejectTag;
        View statusActions;
        View eventActions;
        TextView currentStatus;
        ImageView eventStatus;
        CircleButton changeStatus;
        CircleButton acceptBtn;
        CircleButton rejectBtn;
        View deleteAction;
        View closeAction;
    }

    private Context _context;

    public EventsCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
        _context = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.event_in_list, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.title = (TextView) view.findViewById(R.id.event_tag);
        holder.location = (TextView)view.findViewById(R.id.event_location);
        holder.date = (TextView)view.findViewById(R.id.event_date);
        holder.creator = (TextView) view.findViewById(R.id.event_creator);
        holder.acceptTag = (TextView) view.findViewById(R.id.event_accept);
        holder.rejectTag = (TextView) view.findViewById(R.id.event_reject);
        holder.eventActions = view.findViewById(R.id.actions);
        holder.statusActions = view.findViewById(R.id.event_actions);
        holder.currentStatus = (TextView) view.findViewById(R.id.event_current_status);
        holder.eventStatus = (ImageView)view.findViewById(R.id.event_is_closed);
        holder.changeStatus = (CircleButton)view.findViewById(R.id.event_change_status);
        holder.acceptBtn = (CircleButton)view.findViewById(R.id.event_accept_btn);
        holder.rejectBtn = (CircleButton) view.findViewById(R.id.event_reject_btn);
        holder.deleteAction = view.findViewById(R.id.delete_action);
        holder.closeAction = view.findViewById(R.id.close_action);
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        final String eventId = cursor.getString(cursor.getColumnIndex(Event.EVENT_ID_COLUMN));
        String tag = cursor.getString(cursor.getColumnIndex(Event.TAG_COLUMN));
        long date = cursor.getLong(cursor.getColumnIndex(Event.DATE_COLUMN));
        String location = cursor.getString(cursor.getColumnIndex(Event.LOCATION_COLUMN));
        String creator = cursor.getString(cursor.getColumnIndex(Event.CREATOR_COLUMN));
        boolean isClosed = (cursor.getInt(cursor.getColumnIndex(Event.IS_CLOSED_COLUMN)) == 1);

        ViewHolder holder = (ViewHolder)view.getTag();

        String currentProfileId = Profile.getCurrentProfile().getId();

        holder.title.setText(tag);
        holder.location.setText(location);

        SimpleDateFormat sdf = new SimpleDateFormat("E  dd/MM  HH:mm");
        String d = sdf.format(new Date(date));
        holder.date.setText(d);

        Member member = MembersLoader.getById(creator);

        holder.creator.setText("");
        if(member != null)
        {
            if(member.getFbId().equals(currentProfileId)){
                holder.creator.setText("You create this Event");
            }
            else {
                holder.creator.setText("created by " + member.getFirstName() + " " + member.getLastName());
            }
        }

        List<MemberInEvent> memberInEventList = new Select().from(MemberInEvent.class)
                .where(MemberInEvent.EVENT_ID_COLUMN + " = ?", eventId).execute();

        int acceptCout= 0 , rejectCount = 0;
        int currentProfileStatus = MemberInEvent.NOT_SET_STATUS;

        for (MemberInEvent me : memberInEventList) {

            if(me.getFbId().equals(currentProfileId)){
                currentProfileStatus = me.getStatus();
            }
            if(me.getStatus()== MemberInEvent.ACCEPT_STATUS) acceptCout++;
            if(me.getStatus()== MemberInEvent.REJECT_STATUS) rejectCount++;
        }

        holder.acceptTag.setText(acceptCout + "");
        holder.rejectTag.setText(rejectCount + "");

        holder.currentStatus.setVisibility(View.GONE);
        if(currentProfileStatus != MemberInEvent.NOT_SET_STATUS){
            holder.currentStatus.setVisibility(View.VISIBLE);
            if(currentProfileStatus == MemberInEvent.MAYBE_STATUS){
                holder.currentStatus.setVisibility(View.VISIBLE);
                if(isClosed) {
                    holder.currentStatus.setText("You did not coming to this event");
                }
            }
            else if(currentProfileStatus == MemberInEvent.ACCEPT_STATUS){
                holder.currentStatus.setText("You accepted this event");
            } else if(currentProfileStatus == MemberInEvent.REJECT_STATUS){
                holder.currentStatus.setText("You rejected this event");
            }
        }

        holder.eventStatus.setImageResource(R.drawable.closed_event);
        holder.changeStatus.setVisibility(View.GONE);
        holder.statusActions.setVisibility(View.GONE);
        holder.eventActions.setVisibility(View.GONE);
        if(!isClosed){
            holder.eventStatus.setImageResource(R.drawable.opened_event);

            if(currentProfileStatus == MemberInEvent.NOT_SET_STATUS){
                holder.statusActions.setVisibility(View.VISIBLE);
                holder.acceptBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MemberInEventStatusTask(eventId).execute(MemberInEvent.ACCEPT_STATUS);
                    }
                });

                holder.rejectBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new MemberInEventStatusTask(eventId).execute(MemberInEvent.REJECT_STATUS);
                    }
                });
            }
            else {

                final int currentProfileStatus2 = currentProfileStatus;
                holder.changeStatus.setVisibility(View.VISIBLE);
                holder.changeStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int newStatus = (currentProfileStatus2 == MemberInEvent.ACCEPT_STATUS ? MemberInEvent.REJECT_STATUS : MemberInEvent.ACCEPT_STATUS);
                        new MemberInEventStatusTask(eventId).execute(newStatus);
                    }
                });
            }

            Member currentMember = MembersLoader.getById(currentProfileId);

            if(currentMember != null && currentMember.getIsAdmin())
            {
                holder.eventActions.setVisibility(View.VISIBLE);

                holder.deleteAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                .setTitle("Delete Event")
                                .setMessage("Are you sure you want to delete ?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        // check if the event does not have games

                                        List<Game> games = new Select().from(Game.class).where(Game.EVENT_ID_COLUMN + " = ?", eventId).execute();

                                        if (games != null && games.size() > 0) {
                                            Snackbar snackbar = Snackbar.make(view, "delete the games of this event and then delete it.", Snackbar.LENGTH_LONG);
                                            snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                                            snackbar.show();
                                        } else {
                                            new EventActionTask().execute(eventId, "delete");
                                        }
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Snackbar snackbar = Snackbar.make(view, "the event reserved.", Snackbar.LENGTH_LONG);
                                        snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                                        snackbar.show();
                                    }
                                });

                        builder.create().show();
                    }
                });

                holder.closeAction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // check if the event does not have an opened game
                        List<Game> games = new Select().from(Game.class)
                                .where(Game.EVENT_ID_COLUMN + "= ? and (" + Game.START_TIME_COLUMN + " = ? or " + Game.END_TIME_COLUMN + " = ?)",
                                        eventId, Game.NOT_STARTED_GAME_TIME
                                        , Game.NOT_FINISHED_GAME_TIME).execute();

                        if (games != null && games.size() > 0) {
                            Snackbar snackbar = Snackbar.make(view, "you cannot close event with opened games.", Snackbar.LENGTH_LONG);
                            snackbar.getView().setBackgroundResource(R.color.colorPrimaryDark);
                            snackbar.show();
                        } else {
                            new EventActionTask().execute(eventId, "close");
                        }
                    }
                });
            }
        }
    }

    public class EventActionTask extends AsyncTask<String,Void,Void>{

        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(_context);
        }
        @Override
        protected Void doInBackground(String... params) {
            // first param is event id
            // second param is the action
            if(params.length >= 2){

                String eventId = params[0];
                String action = params[1];
                String url = _pref.getString(_context.getString(R.string.pref_server_url), "http://localhost");
                try {
                    switch (action){
                        case "delete" :
                            // send to server
                            Utilities.sendRequest(url + "/deleteEvent", "DELETE", new JSONObject().put("eventid",eventId));
                            break;
                        case "close" :
                            Utilities.sendRequest(url + "/updateEvent", "POST", new JSONObject().put("eventid",eventId).put("isClosed", true));
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

    public class MemberInEventStatusTask extends AsyncTask<Integer,Void,Void>{

        String eventId;
        public MemberInEventStatusTask(String eventId){
            this.eventId = eventId;
        }
        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(_context);
        }

        @Override
        protected Void doInBackground(Integer... params) {

            // send to server
            MemberInEvent memberInEvent = new MemberInEvent(Profile.getCurrentProfile().getId(), eventId, params[0]);

            try {
                // send to the server
                String url = _pref.getString(_context.getString(R.string.pref_server_url), "http://localhost");
                Utilities.sendRequest(url + "/memberInEventChangeStatus", "POST", new JSONObject(Utilities.mapper.writeValueAsString(memberInEvent)));
            }
            catch (Exception e){
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

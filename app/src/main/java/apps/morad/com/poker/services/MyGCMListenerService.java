package apps.morad.com.poker.services;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.activeandroid.query.Update;
import com.facebook.Profile;
import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.models.MemberInGame;

/**
 * Created by Morad on 1/26/2016.
 */
public class MyGCMListenerService extends GcmListenerService {

    public enum MessageTopics {
        Sync("sync"), Notification("notification");

        String _type;
        MessageTopics(String type){
            _type = type;
        }

        public String getType(){
            return _type;
        }

        public static String[] stringValues() {
            String[] strs = new String[values().length];

            int index = 0;
            for (MessageTopics topic :
                    values()) {
                strs[index] = topic._type;
                index++;
            }

            return strs;
        }
    }

    private static final String TAG = "MyGcmListenerService";

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (from.startsWith("/topics/")) {
            // message received from some topic.
            String topicType = from.substring("/topics/".length());

            if(topicType.equals(MessageTopics.Sync.getType())){
                syncDataWithServer(data);
            }else if(topicType.equals(MessageTopics.Notification.getType())){
                sendNotification(data);
            }

        } else {
            // message to the user

        }
    }

    private void syncDataWithServer(Bundle data) {
        try {
            String op = data.getString("op"); // add / delete / update
            String type = data.getString("type");
            JSONObject obj = new JSONObject(data.getString("data"));

            if(op.equals("add")){
                addObject(type, obj);
            } else if(op.equals("delete")){
                deleteObject(type, obj);
            } else if(op.equals("update")){
                updateObject(type, obj);
            }
        } catch (JSONException e) {
            Log.d("GCM_UPDATER", data.toString());
        }
    }

    private void updateObject(String type, JSONObject obj) throws JSONException{
        if(type.equals("Members")){

            Member member = new Select().from(Member.class).where(Member.FB_ID_COLUMN + "=?", obj.getString(Member.FB_ID_COLUMN)).executeSingle();

            if(member != null) {

                member.updateFromJson(obj);
                member.save();
                notifyUpdate(MainActivity.MEMBERS_UPDATED);
            }

        }else if(type.equals("Events")){

            Event event = new Select().from(Event.class).where(Event.EVENT_ID_COLUMN + "=?", obj.getString(Event.EVENT_ID_COLUMN)).executeSingle();

            if(event != null) {

                event.updateFromJson(obj);
                event.save();
                notifyUpdate(MainActivity.EVENTS_UPDATED);
            }
        }else if(type.equals("Games")){

            Game game = new Select().from(Game.class).where(Game.GAME_ID_COLUMN + "=?", obj.getString(Game.GAME_ID_COLUMN)).executeSingle();
            if(game != null){
                game.updateFromJson(obj);
                game.save();
                notifyUpdate(MainActivity.GAMES_UPDATED);
            }

        }else if(type.equals("MembersInEvents")){
            new Update(MemberInEvent.class).set(MemberInEvent.STATUS_COLUMN + " = " + obj.getInt(MemberInEvent.STATUS_COLUMN))
                    .where(MemberInEvent.EVENT_ID_COLUMN + "= ?", obj.getString(MemberInEvent.EVENT_ID_COLUMN))
                    .where(MemberInEvent.FB_ID_COLUMN + "= ?", obj.getString(MemberInEvent.FB_ID_COLUMN))
                    .execute();

            notifyUpdate(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        }else if(type.equals("MemberInGame")){

            MemberInGame mig = new Select()
                    .from(MemberInGame.class)
                    .where(MemberInGame.GAME_ID_COLUMN + "= ? and ("
                                    + MemberInGame.FB_ID_COLUMN + "= ? and " + MemberInGame.ROUND_COLUMN + "= ?)",
                            obj.getString(MemberInGame.GAME_ID_COLUMN),
                            obj.getString(MemberInGame.FB_ID_COLUMN),
                            obj.getInt(MemberInGame.ROUND_COLUMN))
                    .executeSingle();

            if(mig != null){
                mig.setScore(obj.getInt(MemberInGame.SCORE_COLUMN));
                mig.setOrder(obj.getInt(MemberInGame.ORDER_ID_COLUMN));
                mig.save();
            }

            notifyUpdate(MainActivity.MEMBERS_IN_GAMES_UPDATED);
        }
    }

    private void deleteObject(String type, JSONObject obj) throws JSONException{
        if(type.equals("Events")){
            new Delete().from(MemberInEvent.class)
                    .where(MemberInEvent.EVENT_ID_COLUMN + "=?", obj.getString(Event.EVENT_ID_COLUMN))
                    .execute();
            new Delete().from(Event.class)
                    .where(Event.EVENT_ID_COLUMN + "= ?", obj.getString(Event.EVENT_ID_COLUMN))
                    .execute();

            notifyUpdate(MainActivity.EVENTS_UPDATED);
        }else if(type.equals("Games")){
            new Delete().from(MemberInGame.class)
                    .where(MemberInGame.GAME_ID_COLUMN + "=?", obj.getString(Game.GAME_ID_COLUMN))
                    .execute();
            new Delete().from(Game.class)
                    .where(Game.GAME_ID_COLUMN + "= ?", obj.getString(Game.GAME_ID_COLUMN))
                    .execute();

            notifyUpdate(MainActivity.GAMES_UPDATED);
        }else if(type.equals("MembersInEvents")){
            new Delete().from(MemberInEvent.class)
                    .where(MemberInEvent.EVENT_ID_COLUMN + "=?", obj.getString(MemberInEvent.EVENT_ID_COLUMN))
                    .where(MemberInEvent.FB_ID_COLUMN + "=?", obj.getString(MemberInEvent.FB_ID_COLUMN))
                    .execute();

            notifyUpdate(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        }else if(type.equals("MemberInGame")){
            new Delete().from(MemberInGame.class)
                    .where(MemberInGame.GAME_ID_COLUMN + "=?", obj.getString(MemberInGame.GAME_ID_COLUMN))
                    .where(MemberInGame.FB_ID_COLUMN + "=?", obj.getString(MemberInGame.FB_ID_COLUMN))
                    .where(MemberInGame.ROUND_COLUMN +"=?", obj.getInt(MemberInGame.ORDER_ID_COLUMN))
                    .execute();

            notifyUpdate(MainActivity.MEMBERS_IN_GAMES_UPDATED);
        }
    }

    private void addObject(String type, JSONObject obj) throws JSONException {
        if(type.equals("Members")){
            Member mem = new Member(obj.getString(Member.FB_ID_COLUMN),
                    obj.getString(Member.FIRST_NAME_COLUMN), obj.getString(Member.LAST_NAME_COLUMN),
                    obj.getInt(Member.SCORE_COLUMN), obj.getString(Member.MEDAL_COLUMN), obj.getBoolean(Member.IS_ADMIN_COLUMN));
            mem.save();
            notifyUpdate(MainActivity.MEMBERS_UPDATED);

        }else if(type.equals("Events")){
            Event mem = new Event(obj.getString(Event.EVENT_ID_COLUMN),
                    obj.getLong(Event.CREATED_DATE_COLUMN), obj.getLong(Event.DATE_COLUMN),
                    obj.getString(Event.LOCATION_COLUMN), obj.getString(Event.TAG_COLUMN), obj.getString(Event.CREATOR_COLUMN));
            mem.setIsClosed(false);
            mem.save();
            notifyUpdate(MainActivity.EVENTS_UPDATED);

        }else if(type.equals("Games")){

            Game mem = new Game(obj.getString(Game.GAME_ID_COLUMN),obj.getString(Game.EVENT_ID_COLUMN),
                    obj.getString(Game.GAME_NAME_COLUMN), obj.getBoolean(Game.IS_CONSIDERED_COLUMN), obj.getInt(Game.ORDER_ID_COLUMN));
            mem.save();

            if(obj.has("members")){
                // save members in game
                JSONArray members = (JSONArray) obj.get("members");

                for(int index = 0 ; index < members.length(); index ++){
                    String mobj = (String) members.get(index);
                    MemberInGame mig = new MemberInGame(mobj, obj.getString(Game.GAME_ID_COLUMN));
                    mig.save();
                }
            }

            notifyUpdate(MainActivity.GAMES_UPDATED);

        }else if(type.equals("MembersInEvents")){
            MemberInEvent mem = new MemberInEvent(obj.getString(MemberInEvent.FB_ID_COLUMN),obj.getString(MemberInEvent.EVENT_ID_COLUMN),
                    obj.getInt(MemberInEvent.STATUS_COLUMN));

            mem.save();
            notifyUpdate(MainActivity.MEMBERS_IN_EVENTS_UPDATED);


        }else if(type.equals("MemberInGame")){
            MemberInGame mem = new MemberInGame(obj.getString(MemberInGame.FB_ID_COLUMN),
                    obj.getString(MemberInGame.GAME_ID_COLUMN));
            mem.setRound(obj.getInt(MemberInGame.ROUND_COLUMN)).save();
            notifyUpdate(MainActivity.MEMBERS_IN_GAMES_UPDATED);
        }
    }

    private void notifyUpdate(String updateType) {
        Intent updateIntent = new Intent(updateType);
        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }

    private void sendNotification(Bundle message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.poker_notification_icon)
                .setContentTitle(message.getString("title"))
                .setContentText(message.getString("content"))
                .setAutoCancel(true)
                .setColor(Color.parseColor("#303F9F"))
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(message.getInt("id"), notificationBuilder.build());
    }

    private void syncData(){

        if(Profile.getCurrentProfile() != null) {

            Bundle settingsBundle = new Bundle();
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_MANUAL, true);
            settingsBundle.putBoolean(
                    ContentResolver.SYNC_EXTRAS_EXPEDITED, true);

            String accountType = getString(R.string.account_type);
            ContentResolver.requestSync(new Account(Profile.getCurrentProfile().getName(), accountType),
                    getString(R.string.authority), settingsBundle);
        }
    }
}
package apps.morad.com.poker.syncAdapters;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.activeandroid.query.Select;
import com.activeandroid.query.Update;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.MainActivity;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.Member;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 1/24/2016.
 */
public class DataSyncAdapter extends AbstractThreadedSyncAdapter {

    ContentResolver _resolver;

    public DataSyncAdapter(Context context, boolean autoInitialize){
        super(context, autoInitialize);
        _resolver = context.getContentResolver();
    }

    public DataSyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSync){
        super(context, autoInitialize, allowParallelSync);
        _resolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        // on sync get all members
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String url = pref.getString(getContext().getString(R.string.pref_server_url), "http://localhost");
        long lastSync = pref.getLong(getContext().getString(R.string.pref_last_sync), -1);

        try{
            JSONObject res = Utilities.sendRequest(url + "/sync", "POST", new JSONObject().put("lastSync", lastSync));

            JSONArray array;
            if(res.has("members") && (array = (JSONArray) res.get("members")).length() > 0){
                syncMembers(array);
            }

            if(res.has("events") && (array = (JSONArray) res.get("events")).length() > 0){
                syncEvents(array);
            }

            if(res.has("games") && (array = (JSONArray) res.get("games")).length() > 0){
                syncGames(array);
            }

            if(res.has("membersInEvents") && (array = (JSONArray) res.get("membersInEvents")).length() > 0){
                syncMembersInEvent(array);
            }

            if(res.has("membersInGames") && (array = (JSONArray) res.get("membersInGames")).length() > 0){
                syncMembersInGames(array);
            }

            PreferenceManager.getDefaultSharedPreferences(getContext())
                    .edit().putLong(getContext().getString(R.string.pref_last_sync), System.currentTimeMillis()).commit();
        }
        catch(Exception e){

        }
    }

    private void syncMembersInGames(JSONArray array) throws JSONException {
        for(int index = 0 ; index < array.length(); index++){
            JSONObject mig = array.getJSONObject(index);

            List<MemberInGame> nmig = new Select()
                    .from(MemberInGame.class)
                    .where(MemberInGame.GAME_ID_COLUMN + "= ? and " + MemberInGame.FB_ID_COLUMN + "= ?",
                            mig.getString(MemberInGame.GAME_ID_COLUMN),
                            mig.getString(MemberInGame.FB_ID_COLUMN))
                    .execute();



            if(nmig == null || nmig.size() == 0){ // new member in game
                new MemberInGame(mig.getString(MemberInGame.FB_ID_COLUMN), mig.getString(MemberInGame.GAME_ID_COLUMN))
                        .setOrder(mig.getInt(MemberInGame.ORDER_ID_COLUMN))
                        .setScore(mig.getInt(MemberInGame.SCORE_COLUMN))
                        .save();
            }
            else {
                int newOrder = mig.getInt(MemberInGame.ORDER_ID_COLUMN);

            }
        }

        Intent updateIntent = new Intent(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent);
    }

    private void syncMembersInEvent(JSONArray array) throws JSONException {
        for(int index = 0 ; index < array.length(); index++){
            JSONObject mie = array.getJSONObject(index);

            MemberInEvent nMie = new Select().from(MemberInEvent.class)
                    .where(MemberInEvent.EVENT_ID_COLUMN + "=?", mie.getString(MemberInEvent.EVENT_ID_COLUMN))
                    .where(MemberInEvent.FB_ID_COLUMN + "=?", mie.getString(MemberInEvent.FB_ID_COLUMN))
                    .executeSingle();

            if(nMie == null){ // new event
                nMie = new MemberInEvent(mie.getString(MemberInEvent.FB_ID_COLUMN),
                        mie.getString(MemberInEvent.EVENT_ID_COLUMN), mie.getInt(MemberInEvent.STATUS_COLUMN));

                nMie.save();
            }
            else {
                new Update(MemberInEvent.class).set(MemberInEvent.STATUS_COLUMN + " = " + mie.getInt(MemberInEvent.STATUS_COLUMN))
                        .where(MemberInEvent.EVENT_ID_COLUMN + "= ?", mie.getString(MemberInEvent.EVENT_ID_COLUMN))
                        .where(MemberInEvent.FB_ID_COLUMN + "= ?", mie.getString(MemberInEvent.FB_ID_COLUMN))
                        .execute();
            }
        }

        Intent updateIntent = new Intent(MainActivity.MEMBERS_IN_EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent);
    }

    private void syncGames(JSONArray games) throws JSONException {
        for(int index = 0 ; index < games.length(); index++){
            JSONObject game = games.getJSONObject(index);

            Game gme = new Select().from(Game.class)
                    .where(Game.GAME_ID_COLUMN + "=?", game.getString(Game.GAME_ID_COLUMN))
                    .executeSingle();

            if(gme == null){ // new game
                gme = new Game(game.getString(Game.GAME_ID_COLUMN), game.getString(Game.EVENT_ID_COLUMN),
                        game.getString(Game.GAME_NAME_COLUMN), game.getBoolean(Game.IS_CONSIDERED_COLUMN),
                        game.getInt(Game.ORDER_ID_COLUMN));

                gme.save();
            }
            else {
                gme.updateFromJson(game);
                gme.save();
            }
        }

        Intent updateIntent = new Intent(MainActivity.GAMES_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent);
    }

    private void syncEvents(JSONArray events) throws JSONException {
        for(int index = 0 ; index < events.length(); index++){
            JSONObject event = events.getJSONObject(index);

            Event evnt = new Select().from(Event.class)
                    .where(Event.EVENT_ID_COLUMN + "=?", event.getString(Event.EVENT_ID_COLUMN))
                    .executeSingle();

            if(evnt == null){ // new event
                evnt = new Event(event.getString(Event.EVENT_ID_COLUMN), event.getLong(Event.CREATED_DATE_COLUMN),
                        event.getLong(Event.DATE_COLUMN), event.getString(Event.LOCATION_COLUMN),
                        event.getString(Event.TAG_COLUMN), event.getString(Event.CREATOR_COLUMN));

                evnt.save();
            }
            else {
                evnt.updateFromJson(event);
                evnt.save();
            }
        }

        Intent updateIntent = new Intent(MainActivity.EVENTS_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent);
    }

    private void syncMembers(JSONArray members) throws JSONException {
        for(int index = 0 ; index < members.length(); index++){
            JSONObject member = members.getJSONObject(index);

            Member mem = new Select().from(Member.class)
                    .where(Member.FB_ID_COLUMN + "=?", member.getString(Member.FB_ID_COLUMN))
                    .executeSingle();

            if(mem == null){ // new member
                mem = new Member(member.getString(Member.FB_ID_COLUMN),
                        member.getString(Member.FIRST_NAME_COLUMN), member.getString(Member.LAST_NAME_COLUMN),
                        member.getInt(Member.SCORE_COLUMN), member.getString(Member.MEDAL_COLUMN), member.getBoolean(Member.IS_ADMIN_COLUMN));
                mem.save();
            }
            else {
                mem.updateFromJson(member);
                mem.save();
            }
        }

        Intent updateIntent = new Intent(MainActivity.MEMBERS_UPDATED);
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(updateIntent);
    }
}

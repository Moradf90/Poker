package apps.morad.com.poker.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import com.activeandroid.query.Select;
import com.facebook.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.models.MemberInGame;
import apps.morad.com.poker.models.builders.EventBuilder;
import apps.morad.com.poker.thirdParty.HorizontalListView;
import apps.morad.com.poker.utilities.Utilities;

public class AddOrUpdateGameActivity extends Activity {

    ArrayList<String> membersInEvent;
    ArrayList<String> membersInGame;

    MemberInEventAdapter memberInEventAdapter;
    MemberInEventAdapter memberInGameAdapter;

    GridView membersInTheGameGrid;

    GridView membersInTheEventList;
    String eventId;
    TextView _title;
    Switch _isConsidered;
    ProgressDialog prog;
    SaveEventTask saveEventTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_update_game);

        membersInTheEventList = (GridView)findViewById(R.id.members_accept_event);
        membersInTheGameGrid = (GridView) findViewById(R.id.members_in_the_game);

        _title = (TextView) findViewById(R.id.game_name);
        _isConsidered = (Switch) findViewById(R.id.game_considered);

        eventId = getIntent().getExtras().getString(Game.EVENT_ID_COLUMN);

        List<MemberInEvent> memberInEvents = new Select().from(MemberInEvent.class)
                .where(MemberInEvent.EVENT_ID_COLUMN + "= ?"
                + " and " + MemberInEvent.STATUS_COLUMN + "= ?", eventId, MemberInEvent.ACCEPT_STATUS)
                .execute();

        membersInEvent = new ArrayList<>();
        membersInGame = new ArrayList<>();

        if(memberInEvents != null && memberInEvents.size() > 0){
           for(MemberInEvent mie : memberInEvents){
               membersInEvent.add(mie.getFbId());
           }
        }

        memberInEventAdapter = new MemberInEventAdapter(this, membersInEvent);
        memberInGameAdapter = new MemberInEventAdapter(this, membersInGame);

        membersInTheGameGrid.setAdapter(memberInGameAdapter);
        membersInTheEventList.setAdapter(memberInEventAdapter);

        membersInTheEventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fbId = (String) parent.getAdapter().getItem(position);

                membersInGame.add(fbId);
                membersInEvent.remove(membersInEvent.indexOf(fbId));

                memberInEventAdapter.notifyDataSetChanged();
                memberInGameAdapter.notifyDataSetChanged();

            }
        });

        membersInTheGameGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fbId = (String) parent.getAdapter().getItem(position);

                membersInEvent.add(fbId);
                membersInGame.remove(membersInGame.indexOf(fbId));

                memberInEventAdapter.notifyDataSetChanged();
                memberInGameAdapter.notifyDataSetChanged();

            }
        });

        findViewById(R.id.game_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validate()) {
                    save();
                }
            }
        });

        prog = new ProgressDialog(this);
        prog.setCancelable(false);
        prog.setMessage("Saving ...");
    }

    private boolean validate(){
        boolean isValid = true;

        if(_title.getText().length() == 0){
            isValid = false;
            _title.setError("enter name");
        }

        if(membersInGame.size() >= Game.MIN_NUMBER_OF_MEMBERS) {

            if (_isConsidered.isChecked() && membersInGame.size() < Game.MIN_NUMBER_OF_MEMBERS_FOR_CONSIDERED_GAME) {
                isValid = false;
                Snackbar snackbar = Snackbar.make(_title, "Not enough members checked for considered game", Snackbar.LENGTH_LONG);
                snackbar.getView().setBackgroundResource(R.color.redColor);
                snackbar.show();
            }
        }
        else {
            isValid = false;
            Snackbar snackbar = Snackbar.make(_title, "Not enough members checked for this game", Snackbar.LENGTH_LONG);
            snackbar.getView().setBackgroundResource(R.color.redColor);
            snackbar.show();
        }
        return isValid;
    }

    private void save(){
        saveEventTask = new SaveEventTask();
        saveEventTask.execute();
    }

    public class SaveEventTask extends AsyncTask<Void, Void, Boolean> {

        SaveEventTask() {
        }

        Game game;
        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String id = "g_" + new SecureRandom().nextLong();

            List<Game> games = new Select().from(Game.class)
                    .where(Game.EVENT_ID_COLUMN + " = ?", eventId).execute();

            game = new Game(id, eventId, _title.getText().toString(), _isConsidered.isChecked(), games.size() + 1);

            prog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // send to the server
                JSONObject data = new JSONObject(Utilities.mapper.writeValueAsString(game));
                data.put("members", new JSONArray(membersInGame));
                String url = _pref.getString(getString(R.string.pref_server_url), "http://localhost");
                JSONObject res = Utilities.sendRequest(url + "/addGame", "POST", data);
                return res.has("isCreated") && res.getBoolean("isCreated");
            }
            catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            prog.hide();
            saveEventTask = null;

            if (success) {
                finish();
            } else {

            }
        }

        @Override
        protected void onCancelled() {
            saveEventTask = null;
            prog.hide();
        }
    }
}

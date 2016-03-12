package apps.morad.com.poker.fragments;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import apps.morad.com.poker.R;
import apps.morad.com.poker.activities.EventDetailsActivity;
import apps.morad.com.poker.adapters.MemberInEventAdapter;
import apps.morad.com.poker.models.Game;
import apps.morad.com.poker.models.MemberInEvent;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 3/11/2016.
 */
public class AddOrUpdateGameDialogFragment extends DialogFragment {

    public static final String TAG = "addOrUpdateGame";
    public static AddOrUpdateGameDialogFragment newInstance(){
        return new AddOrUpdateGameDialogFragment();
    }

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_or_update_game, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        getDialog().setTitle("New Game");

        membersInTheEventList = (GridView)view.findViewById(R.id.members_accept_event);
        membersInTheGameGrid = (GridView) view.findViewById(R.id.members_in_the_game);

        _title = (TextView) view.findViewById(R.id.game_name);
        _isConsidered = (Switch) view.findViewById(R.id.game_considered);

        eventId = EventDetailsActivity.getEvent().getEventId();

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

        memberInEventAdapter = new MemberInEventAdapter(getActivity(), membersInEvent);
        memberInGameAdapter = new MemberInEventAdapter(getActivity(), membersInGame);

        membersInTheGameGrid.setAdapter(memberInGameAdapter);
        membersInTheEventList.setAdapter(memberInEventAdapter);

        membersInTheEventList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fbId = (String) parent.getAdapter().getItem(position);

                membersInGame.add(fbId);
                membersInEvent.remove(membersInEvent.indexOf(fbId));

                memberInEventAdapter.swapProfiles(membersInEvent);
                memberInGameAdapter.swapProfiles(membersInGame);

            }
        });

        membersInTheGameGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fbId = (String) parent.getAdapter().getItem(position);

                membersInEvent.add(fbId);
                membersInGame.remove(membersInGame.indexOf(fbId));

                memberInEventAdapter.swapProfiles(membersInEvent);
                memberInGameAdapter.swapProfiles(membersInGame);

            }
        });

        view.findViewById(R.id.game_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (validate()) {
                    save();
                }
            }
        });

        prog = new ProgressDialog(getActivity());
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
        String _url;
        @Override
        protected void onPreExecute() {
            _url = getActivity().getString(R.string.server_url);
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
                JSONObject res = Utilities.sendRequest(_url + "/addGame", "POST", data);
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
                dismiss();
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

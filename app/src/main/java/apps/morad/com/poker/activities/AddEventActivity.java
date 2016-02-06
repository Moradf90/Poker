package apps.morad.com.poker.activities;

import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.app.Activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.facebook.Profile;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.builders.EventBuilder;
import apps.morad.com.poker.utilities.Utilities;

public class AddEventActivity extends Activity{

    private static final int DATE_DIALOG_ID = 0;
    private static final int TIME_DIALOG_ID = 1;

    TextView _title, _location;
    Button _date, _time;
    Date _selectedDate;
    boolean isDateSelected = false, isTimeSelected = false;

    ProgressDialog prog;
    SaveEventTask saveEventTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        _selectedDate = new Date();
        _title = (TextView)findViewById(R.id.event_title);
        _location = (TextView) findViewById(R.id.event_location);
        _date = (Button) findViewById(R.id.event_date_piker);
        _time = (Button) findViewById(R.id.event_time_piker);

        _date.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _date.setError(null);
                showDialog(DATE_DIALOG_ID);
            }
        });

        _time.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                _time.setError(null);
                showDialog(TIME_DIALOG_ID);
            }
        });

        (findViewById(R.id.event_save_btn)).setOnClickListener(new OnClickListener() {
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

    private void save(){
        saveEventTask = new SaveEventTask();
        saveEventTask.execute();
    }

    private boolean validate(){

        boolean isValid = true;

        if(_title.getText().length() <= 0)
        {
            _title.setError("enter event title");
            isValid = false;
        }

        if(_selectedDate.getTime() < System.currentTimeMillis())
        {
            _date.setError("wrong date");
            _time.setError("wrong time");
            isValid = false;
        }

        if(!isDateSelected){
            _date.setError("enter date");
            isValid = false;
        }

        if(!isTimeSelected){
            _time.setError("wrong time");
            isValid = false;
        }

        if(_location.getText().length() <=0)
        {
            _location.setError("enter event location");
            isValid = false;
        }

        return isValid;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        final Calendar c = Calendar.getInstance();
        final SimpleDateFormat formatDate = new SimpleDateFormat("EEEE dd/MM/yy");
        final SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");
        switch (id)
        {
            case DATE_DIALOG_ID : return new DatePickerDialog(this, R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                    long d = Date.parse(String.format("%d/%d/%d", monthOfYear + 1, dayOfMonth, year));
                    _selectedDate.setTime(d);
                    _date.setText(formatDate.format(_selectedDate));
                    _date.setTextColor(getResources().getColor(R.color.colorPrimary));

                    isDateSelected = true;
                }
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

            case TIME_DIALOG_ID : return new TimePickerDialog(this, R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    _selectedDate.setHours(hourOfDay);
                    _selectedDate.setMinutes(minute);
                    _time.setTextColor(getResources().getColor(R.color.colorPrimary));
                    _time.setText(formatTime.format(_selectedDate));
                    isTimeSelected = true;
                }
            }, 21, 0, true);
        }
        return null;
    }

    public class SaveEventTask extends AsyncTask<Void, Void, Boolean> {

        SaveEventTask() {

        }

        Event newEvent;
        SharedPreferences _pref;
        @Override
        protected void onPreExecute() {
            _pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            newEvent = EventBuilder.instance().setCreatedDate(new Date().getTime())
                    .setDate(_selectedDate.getTime())
                    .setCreator(Profile.getCurrentProfile().getId())
                    .setLocation(_location.getText().toString())
                    .setTag(_title.getText().toString()).build();

            prog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try{
                // send to the server
                String url = _pref.getString(getString(R.string.pref_server_url), "http://localhost");
                JSONObject res = Utilities.sendRequest(url + "/addEvent", "POST", new JSONObject(Utilities.mapper.writeValueAsString(newEvent)));
                return res.has("isCreated") && res.getBoolean("isCreated");
            }
            catch (Exception e){
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


package apps.morad.com.poker.fragments;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.facebook.Profile;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import apps.morad.com.poker.R;
import apps.morad.com.poker.models.Event;
import apps.morad.com.poker.models.builders.EventBuilder;
import apps.morad.com.poker.utilities.Utilities;

/**
 * Created by Morad on 2/21/2016.
 */
public class AddOrUpdateEventDialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{

    public static final String TAG = "addOrUpdateEvent";

    public static AddOrUpdateEventDialogFragment newInstance(){
        return new AddOrUpdateEventDialogFragment();
    }

    private TextView _title, _location;
    private Button _date, _time;
    private Date _selectedDate;
    private boolean isDateSelected = false, isTimeSelected = false;
    private SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy");
    private SimpleDateFormat formatTime = new SimpleDateFormat("kk:mm");
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_add_or_update_event, container, false);
        getDialog().setTitle("New Event");

        _selectedDate = new Date();
        _title = (TextView) v.findViewById(R.id.event_title);
        _location = (TextView) v.findViewById(R.id.event_location);
        _date = (Button) v.findViewById(R.id.event_date_piker);
        _time = (Button) v.findViewById(R.id.event_time_piker);

        v.findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    save();
                }
            }
        });

        v.findViewById(R.id.cancel_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        _date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _date.setError(null);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("DatePickerDialogFragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                // Create and show the dialog.
                DatePickerDialogFragment newFragment = new DatePickerDialogFragment();
                newFragment.setListener(AddOrUpdateEventDialogFragment.this);
                newFragment.setCancelable(false);
                newFragment.show(ft, "DatePickerDialogFragment");
            }
        });

        _time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _time.setError(null);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("TimePickerDialogFragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                // Create and show the dialog.
                TimePickerDialogFragment newFragment = new TimePickerDialogFragment();
                newFragment.setListener(AddOrUpdateEventDialogFragment.this);
                newFragment.setCancelable(false);
                newFragment.show(ft, "TimePickerDialogFragment");
            }
        });

        return v;
    }

    private void save() {
        new SaveEventTask().execute();
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

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        _selectedDate.setHours(hourOfDay);
        _selectedDate.setMinutes(minute);
        _time.setText(formatTime.format(_selectedDate));
        isTimeSelected = true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        long d = Date.parse(String.format("%d/%d/%d", monthOfYear + 1, dayOfMonth, year));
        _selectedDate.setTime(d);
        _date.setText(formatDate.format(_selectedDate));

        isDateSelected = true;
    }

    public class SaveEventTask extends AsyncTask<Void, Void, Boolean> {

        private Event newEvent;
        private ProgressDialog prog;
        @Override
        protected void onPreExecute() {
            prog = new ProgressDialog(getActivity());
            prog.setCancelable(false);
            prog.setMessage("Saving ...");

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
                JSONObject res = Utilities.sendRequest(getString(R.string.server_url) + "/addEvent", "POST", new JSONObject(Utilities.mapper.writeValueAsString(newEvent)));
                return res.has("isCreated") && res.getBoolean("isCreated");
            }
            catch (Exception e){
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean success) {

            prog.hide();
            dismiss();
        }

        @Override
        protected void onCancelled() {
            prog.hide();
        }
    }
}

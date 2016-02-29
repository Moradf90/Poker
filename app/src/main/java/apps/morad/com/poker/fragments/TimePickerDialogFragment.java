package apps.morad.com.poker.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by Morad on 2/22/2016.
 */
public class TimePickerDialogFragment extends DialogFragment
{

    private TimePickerDialog.OnTimeSetListener _listener;

    public TimePickerDialogFragment(){}

    public void setListener(TimePickerDialog.OnTimeSetListener listerner){
        _listener = listerner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), _listener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}
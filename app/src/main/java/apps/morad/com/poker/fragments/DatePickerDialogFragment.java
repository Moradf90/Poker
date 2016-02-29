package apps.morad.com.poker.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import java.util.Calendar;

/**
 * Created by Morad on 2/22/2016.
 */
public class DatePickerDialogFragment extends DialogFragment
{
    private DatePickerDialog.OnDateSetListener _listener;

    public void setListener(DatePickerDialog.OnDateSetListener listerner){
        _listener = listerner;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), _listener, year, month, day);
    }
}

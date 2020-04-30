package com.example.assignment1;

import android.app.TimePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Return the selected value to the specified view and control.
        //int viewID = this.getArguments().getInt("viewID");
        int controlID = this.getArguments().getInt("controlID");

        if (controlID != 0) {
            Calendar cal = Calendar.getInstance();
            cal.set(cal.HOUR_OF_DAY,hourOfDay);
            cal.set(cal.MINUTE, minute);

            SimpleDateFormat tvFormat = new SimpleDateFormat("h:mm a");
            String tvString = tvFormat.format(cal.getTime());

            EditText et = getActivity().findViewById(controlID);
            et.setText(tvString, TextView.BufferType.EDITABLE);
            et.clearFocus();
        }
    }
}


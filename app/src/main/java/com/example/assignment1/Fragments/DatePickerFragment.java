package com.example.assignment1.Fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Return the selected value to the specified view and control.
        //int viewID = this.getArguments().getInt("viewID");
        int controlID = this.getArguments().getInt("controlID");

        if (controlID != 0) {
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, day);

            SimpleDateFormat dtFormat = new SimpleDateFormat("dd-MM-yyyy");
            String dtString = dtFormat.format(cal.getTime());

            EditText et = getActivity().findViewById(controlID);
            et.setText(dtString, TextView.BufferType.EDITABLE);
            et.clearFocus();
        }
    }
}
package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
    }

    // Called when the user taps the Search button
    public void onBackClick(View view) {
        super.onBackPressed();
    }

    public void onStartDateClick(View view) {
        EditText et = findViewById(R.id.startDate);
        String strVal = et.getText().toString();

        if (strVal.matches("")) {
            int controlID = R.id.startDate;

            Bundle bundle = new Bundle();
            bundle.putInt("controlID",controlID);

            showDatePickerDialog(view, bundle);
        }
    }

    public void onEndDateClick(View view) {
        EditText et = findViewById(R.id.endDate);
        String strVal = et.getText().toString();

        if (strVal.matches("")) {
            int controlID = R.id.endDate;

            Bundle bundle = new Bundle();
            bundle.putInt("controlID",controlID);

            showDatePickerDialog(view, bundle);
        }
    }

    public void onStartTimeClick(View view) {
        EditText et = findViewById(R.id.startTime);
        String strVal = et.getText().toString();

        if (strVal.matches("")) {
            int controlID = R.id.startTime;

            Bundle bundle = new Bundle();
            bundle.putInt("controlID",controlID);

            showTimePickerDialog(view, bundle);
        }
    }

    public void onEndTimeClick(View view) {
        EditText et = findViewById(R.id.endTime);
        String strVal = et.getText().toString();

        if (strVal.matches("")) {
            int controlID = R.id.endTime;

            Bundle bundle = new Bundle();
            bundle.putInt("controlID",controlID);

            showTimePickerDialog(view, bundle);
        }
    }

    public void showDatePickerDialog(View view, Bundle bundle) {
        DialogFragment dtFragment = new DatePickerFragment();
        dtFragment.setArguments(bundle);
        dtFragment.show(getSupportFragmentManager(), "datePicker");
    }


    public void showTimePickerDialog(View view, Bundle bundle) {
        DialogFragment tvFragment = new TimePickerFragment();
        tvFragment.setArguments(bundle);
        tvFragment.show(getSupportFragmentManager(), "timePicker");
    }
}
package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.mbms.FileInfo;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.assignment1.Fragments.DatePickerFragment;
import com.example.assignment1.Fragments.TimePickerFragment;
import com.example.assignment1.Util.Filter.ImageFilter;

import java.util.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Predicate;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Get the Intent that started this activity and extract the string.
        Intent intent = getIntent();
    }

    // Called when the user taps the Search button
    public void onBackClick(View view) {
        super.onBackPressed();
    }

    public void onSearchClick(View view) {
        // Initialize list of filters
        //List<Predicate<FileInfo>> filters = new ArrayList<Predicate<FileInfo>>();
        ImageFilter filter = new ImageFilter();

        // Try to get our start date value if it's set
        String startDateString = ((EditText)findViewById(R.id.startDate)).getText().toString();
        if (startDateString.length() > 1) {
            // If we have a date, try to parse it
            Date parseDate = TryParseDate(startDateString + " " + ((EditText)findViewById(R.id.startTime)).getText().toString());
            // If parse was successful, get add a filter for our start date
            filter.StartDate = parseDate;
        }

        // Try to get an end date
        String endDateString = ((EditText)findViewById(R.id.endDate)).getText().toString();
        if (endDateString.length() > 1) {
            // If we have a date, try to parse it
            Date parseDate = TryParseDate(endDateString + " " + ((EditText)findViewById(R.id.endTime)).getText().toString());
            // If parse was successful, get add a filter for our start date
            if(parseDate != null) {
                filter.EndDate = parseDate;
            }
        }

        // Get the caption text
        filter.Caption = ((EditText)findViewById(R.id.searchView)).getText().toString();

        // Get intent
        Intent intent = getIntent();
        // Add filters in our Extra
        intent.putExtra("Filter", filter);
        // Set the result to ok
        setResult(RESULT_OK, intent);
        // END
        finish();
    }

    private Date TryParseDate(String text) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
             return simpleDateFormat.parse(text);
        } catch (ParseException e) {
            return null;
        }
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

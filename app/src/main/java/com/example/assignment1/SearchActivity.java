package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.telephony.mbms.FileInfo;
import android.text.Editable;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Date;
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
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        //TextView textView = findViewById(R.id.textView);
        //textView.setText(message);
    }

    // Called when the user taps the Search button
    public void onBackClick(View view) {
        super.onBackPressed();
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
    }

    public void onSearchClick(View view) {
        // Initialize list of filters
        List<Predicate<FileInfo>> filters = new ArrayList<Predicate<FileInfo>>();

        // Try to get our start date value if it's set
        String startDateString = ((EditText)findViewById(R.id.startDate)).getText().toString();
        if (startDateString.length() > 1) {
            // If we have a date, try to parse it
            Date parseDate = TryParseDate(startDateString+ " " + ((EditText)findViewById(R.id.startTime)).getText().toString());
            // If parse was successful, get add a filter for our start date
            if(parseDate != null) {
                //filters.add(f -> (FileInfo) f.lastModified() < startDate)
            }
        }

        // Try to get an end date
        String endDateString = ((EditText)findViewById(R.id.endDate)).getText().toString();
        if (endDateString.length() > 1) {
            // If we have a date, try to parse it
            Date parseDate = TryParseDate(endDateString+ " " + ((EditText)findViewById(R.id.endTime)).getText().toString());
            // If parse was successful, get add a filter for our start date
            if(parseDate != null) {
                //filters.add(f -> (FileInfo) f.lastModified() > parseDate)
            }
        }

        // Get intent
        Intent intent = new Intent(this, MainActivity.class);
        // Add filters in our Extra
        intent.putExtra("Filters", (Parcelable) filters);
        // Set the result to ok
        setResult(RESULT_OK);
        // END
        finish();
    }

    private Date TryParseDate(String text) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
             return (Date) simpleDateFormat.parse(text);
        } catch (ParseException e) {
            return null;
        }
    }
}

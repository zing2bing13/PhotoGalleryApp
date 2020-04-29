package com.example.assignment1;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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
}

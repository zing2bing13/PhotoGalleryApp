package com.example.assignment1;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import com.example.assignment1.Assertions.RecyclerViewItemCountAssertion;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class ActivityTests {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testFilter() {
        ActivityScenario mainActivity = ActivityScenario.launch(MainActivity.class);

        // TODO: Replace placeholder numbers with view ids
        // Find search button and click it
        onView(withId(R.id.buttonSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.searchActivity)).check(matches(isDisplayed()));

        String dateString = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        // Find the start date text button and enter a date string
        onView(withId(R.id.startDate)).perform(typeText(dateString));

        // Find the end date text button and enter today's date
        onView(withId(R.id.endDate)).perform(typeText(dateString));

        // Find search button and click it
        onView(withId(R.id.searchView)).perform(click());

        // Should only have 1 result
        //onView(withId(R.id.searchActivity)).check(new RecyclerViewItemCountAssertion(1));
    }
}
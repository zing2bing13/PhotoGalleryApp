package com.example.assignment1;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import java.util.UUID;

import com.example.assignment1.Assertions.RecyclerViewItemCountAssertion;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class ActivityTests {
    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void testCaptionFilterFilter() {
        // Launch activity
        activityRule.launchActivity(new Intent());

        //mainActivity.onActivity().
        String captionToSearch = setFirstImageRandomCaption(activityRule.getActivity());

        // Find search button and click it
        onView(withId(R.id.buttonSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.searchActivity)).check(matches(isDisplayed()));

        //String dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // Find the start date text button and enter a date string
        //onView(withId(R.id.startDate)).perform(typeText(dateString));

        // Find the end date text button and enter today's date
        //onView(withId(R.id.endDate)).perform(typeText(dateString));

        onView(withId(R.id.searchView)).perform(typeText(captionToSearch));

        // Find search button and click it
        onView(withId(R.id.submitSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.mainActivity)).check(matches(isDisplayed()));

        // Check if 1 exist
        onView(withId(R.id.imageView)).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidCaptionSearch() {
        // Launch activity
        activityRule.launchActivity(new Intent());

        //mainActivity.onActivity().
        String captionToSearch = UUID.randomUUID().toString();

        // Find search button and click it
        onView(withId(R.id.buttonSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.searchActivity)).check(matches(isDisplayed()));

        // Enter our search string into the caption search
        onView(withId(R.id.searchView)).perform(typeText(captionToSearch));

        // Find search button and click it
        onView(withId(R.id.submitSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.mainActivity)).check(matches(isDisplayed()));

        // Check if 1 exist
        onView(withId(R.id.imageView)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testEmptySearch() {
        // Launch activity
        activityRule.launchActivity(new Intent());

        // Find search button and click it
        onView(withId(R.id.buttonSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.searchActivity)).check(matches(isDisplayed()));

        // Find search button and click it
        onView(withId(R.id.submitSearch)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(R.id.mainActivity)).check(matches(isDisplayed()));

        // Check if 1 exist
        onView(withId(R.id.imageView)).check(matches(isDisplayed()));
    }

    private String setFirstImageRandomCaption(MainActivity mainActivity) {
        // Get first image
        List<String> files = mainActivity.getAllFilePaths();
        if(files == null || files.isEmpty())
            return null;

        // Generate a random guid and set to the caption on the image
        String captionStr = UUID.randomUUID().toString();
        MainActivity.setExifAttr(files.get(0), ExifInterface.TAG_IMAGE_DESCRIPTION, captionStr);

        return captionStr;
    }
}
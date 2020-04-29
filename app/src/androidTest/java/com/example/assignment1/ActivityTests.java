package com.example.assignment1;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        onView(withId(0)).perform(click());

        // Test to see if the Search Activity is in foreground
        onView(withId(1)).check(matches(isDisplayed()));

        // Find the search text text button and enter test string
        onView(withId(2)).perform(typeText("test" ));

        // Find search button and click it
        onView(withId(3)).perform(click());

        // Should only have 1 result
        onView(withId(1)).check(new RecyclerViewItemCountAssertion(1));
    }
}

public class RecyclerViewItemCountAssertion  implements ViewAssertion {
    private final int expectedCount;

    public RecyclerViewItemCountAssertion (int expectedCount) {
        this.expectedCount = expectedCount;
    }

    @Override
    public void check(View view, NoMatchingViewException noViewFoundException) {
        if (noViewFoundException != null) {
            throw noViewFoundException;
        }

        RecyclerView recyclerView = (RecyclerView) view;
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        assertThat(adapter.getItemCount(), Is.is(expectedCount));
    }

}

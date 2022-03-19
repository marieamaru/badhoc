package com.igm.badhoc.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.igm.badhoc.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NotificationDisplayFragmentTest {

    public FragmentScenario<NotificationFragment> fragmentScenario;

    @Before
    public void setup() {
        fragmentScenario = FragmentScenario.launchInContainer(NotificationFragment.class, new Bundle(), R.style.AppTheme_NoActionBar);
    }

    @Test
    @DisplayName("Verify all elements are displayed")
    public void onCreate() {
        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
        onView(withId(R.id.txt_server)).check(matches(isDisplayed()));
        onView(withId(R.id.to_server_button)).check(matches(isDisplayed()));
    }


    @Test
    public void dominatedTitle() {
        onView(withId(R.id.txt_server)).check(matches(withText("Notifications from dominant")));
    }

    @Test
    public void serverButton() {
        onView(withId(R.id.to_server_button)).check(matches(isClickable()));
    }
}

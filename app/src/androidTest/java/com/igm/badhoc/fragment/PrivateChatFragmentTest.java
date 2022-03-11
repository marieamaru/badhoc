package com.igm.badhoc.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.matcher.ViewMatchers;

import com.igm.badhoc.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class PrivateChatFragmentTest {
    public FragmentScenario<PrivateChatFragment> fragmentScenario;

    @Before
    public void setup() {
        fragmentScenario = FragmentScenario.launchInContainer(PrivateChatFragment.class);
    }

    @Test
    @DisplayName("Verify all elements are displayed")
    public void onCreate() {
        onView(withId(R.id.txtMessage)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSend)).check(matches(isDisplayed()));
        onView(withId(R.id.message_list)).check(matches(isDisplayed()));
        onView(withId(R.id.progressBar)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }

    @Test
    @DisplayName("Verify all elements are displayed")
    public void onSend() {
        onView(withId(R.id.btnSend)).perform(click());
    }
}

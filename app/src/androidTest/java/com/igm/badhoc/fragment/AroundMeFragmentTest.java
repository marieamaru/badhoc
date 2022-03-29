package com.igm.badhoc.fragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.igm.badhoc.R;
import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AroundMeFragmentTest {

    public FragmentScenario<AroundMeFragment> fragmentScenario;

    @Before
    public void setup() {
        fragmentScenario = FragmentScenario.launchInContainer(AroundMeFragment.class);
    }

    @Test
    @DisplayName("Verify all elements are displayed")
    public void onCreate() {
        onView(withId(R.id.notif_list)).check(matches(isDisplayed()));
    }

}

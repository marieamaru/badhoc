package com.igm.badhoc.service;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.igm.badhoc.activity.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
class ServerServiceTest {

    @Rule
    public ActivityScenarioRule<MainActivity> mainActivityScenario = new ActivityScenarioRule<>(
            MainActivity.class);

    @Test
    void onStartCommandTest() {
        /*
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final Intent intentService = new Intent(appContext, ServerService.class);
        final String testNode = "{ \"test\" : \"test\" }";
        intentService.putExtra(Tag.ACTION_UPDATE_NODE_INFO.value, testNode);
        appContext.startForegroundService(intentService);
        assertTrue();


         */

    }

    @Test
    void onCreate() {
    }

    @Test
    void onDestroy() {
    }

    @Test
    void publishMessage() {
    }

    @Test
    void subscribeToTopic() {
    }
}
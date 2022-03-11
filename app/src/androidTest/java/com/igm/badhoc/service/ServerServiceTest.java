package com.igm.badhoc.service;

import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.igm.badhoc.activity.MainActivity;
import com.igm.badhoc.model.Tag;
import com.igm.badhoc.util.DeviceUtil;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ServerServiceTest {

    @Test
    public void onStartCommandTest() {
        final Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final Intent intentService = new Intent(appContext, ServerService.class);
        final String testNode = "{ \"test\" : \"test\" }";
        intentService.putExtra(Tag.ACTION_UPDATE_NODE_INFO.value, testNode);
        appContext.startForegroundService(intentService);
        assertTrue(DeviceUtil.isServiceRunning(ServerService.class, appContext));
    }

}
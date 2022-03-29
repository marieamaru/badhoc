package com.igm.badhoc.util;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.igm.badhoc.model.Node;
import com.igm.badhoc.util.DeviceUtil;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class DeviceUtilIntegrationTest {

    @Test
    public void getMacAddressTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        final String macAddressTest = DeviceUtil.getMacAddress();
        final String addressModel = "00:00:00:00:00:00";
        assertThat(macAddressTest).isNotEmpty();
        assertThat(macAddressTest).hasSize(addressModel.length());
    }


    @Test
    public void getLteSignalTest() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Node node = Node.builder("123", "deviceName").build();
        DeviceUtil.getLteSignal(appContext, node);
        assertThat(node.getLteSignal()).isNotEmpty();
        assertThat(Integer.valueOf(node.getLteSignal())).isNegative();
    }

    @Test
    public void isConnectedToInternetTest() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean isConnectedToInternet = DeviceUtil.isConnectedToInternet(appContext);
        assertThat(isConnectedToInternet).isNotNull();
        assertThat(isConnectedToInternet).isTrue();
    }

}
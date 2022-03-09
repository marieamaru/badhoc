package com.igm.badhoc.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DeviceUtilUnitTest {

    @Test
    public void getMacAddressTest() {
        final String macAddress = DeviceUtil.getMacAddress();
        final String addressModel = "00:00:00:00:00:00";
        assertNotNull(macAddress);
        assertThat(macAddress).isNotBlank();
        assertThat(macAddress).isNotEmpty();
        assertThat(macAddress).hasSize(addressModel.length());
    }

    @Test
    public void getRealMacAddressTest() {
        final String realMacAddress = DeviceUtil.getRealMacAddress();
        final String addressModel = "00:00:00:00:00:00";
        assertNotNull(realMacAddress);
        assertThat(realMacAddress).isNotBlank();
        assertThat(realMacAddress).isNotEmpty();
        assertThat(realMacAddress).hasSize(addressModel.length());
    }


}
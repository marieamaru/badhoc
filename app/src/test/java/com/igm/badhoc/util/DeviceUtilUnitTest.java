package com.igm.badhoc.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class DeviceUtilUnitTest {

    @Test
    @DisplayName("Test getMacAddress")
    public void getMacAddressTest() {
        final String macAddress = DeviceUtil.getMacAddress();
        final String addressModel = "00:00:00:00:00:00";
        assertNotNull(macAddress);
        assertThat(macAddress).isNotBlank();
        assertThat(macAddress).isNotEmpty();
        assertThat(macAddress).hasSize(addressModel.length());
    }


}
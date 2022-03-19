package com.igm.badhoc.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class NeighborUnitTest {
    @Test
    @DisplayName("Test for constructor")
    public void test() {
        final String id = "1";
        final int rssi = -1;
        final String macAddress = "00:00:00:00:00:00";
        final Neighbor neighbor = new Neighbor(id, macAddress, rssi);
        assertThat(neighbor).isNotNull();
        assertThat(neighbor.getId()).isEqualTo(id);
        assertThat(neighbor.getMacAddress()).isEqualTo(macAddress);
        assertThat(neighbor.getRSSI()).isEqualTo(rssi);
    }

    @Test
    @DisplayName("Test equal")
    public void equalTest() {
        final String id = "1";
        final int rssi = -1;
        final String macAddress = "00:00:00:00:00:00";
        final Neighbor neighbor1 = new Neighbor(id, macAddress, rssi);
        final Neighbor neighbor2 = new Neighbor(id, macAddress, rssi);
        assertThat(neighbor1).isEqualTo(neighbor2);
    }

    @Test
    @DisplayName("Test not equals")
    public void notEqualTest() {
        final String id = "1";
        final int rssi = -1;
        final String macAddress1 = "00:00:00:00:00:00";
        final String macAddress2 = "00:00:00:00:00:01";
        final Neighbor neighbor1 = new Neighbor(id, macAddress1, rssi);
        final Neighbor neighbor2 = new Neighbor(id, macAddress2, rssi);
        assertThat(neighbor1).isNotEqualTo(neighbor2);
    }

    @Test
    @DisplayName("Test hashcode")
    public void hashTest() {
        final String id = "1";
        final int rssi = -1;
        final String macAddress = "00:00:00:00:00:00";
        final Neighbor neighbor1 = new Neighbor(id, macAddress, rssi);
        final Neighbor neighbor2 = new Neighbor(id, macAddress, rssi);
        assertThat(neighbor1).hasSameHashCodeAs(neighbor2);
    }



}

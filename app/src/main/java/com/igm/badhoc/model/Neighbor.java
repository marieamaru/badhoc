package com.igm.badhoc.model;

import java.util.Objects;

public class Neighbor {
    private String macAddress;
    private float rssi;

    public Neighbor(String macAddress, float rssi) {
        this.macAddress = macAddress;
        this.rssi = rssi;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public float getRssi() {
        return rssi;
    }

    public void setRssi(float rssi) {
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbor neighbor = (Neighbor) o;
        return Float.compare(neighbor.rssi, rssi) == 0 && Objects.equals(macAddress, neighbor.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress, rssi);
    }
}

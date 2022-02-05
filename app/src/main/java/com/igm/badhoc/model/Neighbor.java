package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.util.List;

public class Neighbor {

    private final String deviceName;

    private final String id;

    private int type;

    private boolean isNearby;

    private int speed;

    private float rssi;

    private int status;

    private String macAddress;

    private long latitude;

    private long longitude;

    private List<Neighbor> neighbourNodes;

    private Neighbor(final Builder builder) {
        this.id = builder.id;
        this.deviceName = builder.deviceName;
    }

    public String getId() {
        return id;
    }

    public int getSpeed() {
        return speed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getLatitude() {
        return latitude;
    }

    public void setPosition(long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getLongitude() {
        return longitude;
    }

    public List<Neighbor> getNeighbourNodes() {
        return neighbourNodes;
    }

    public void setNeighbourNodes(List<Neighbor> neighbourNodes) {
        this.neighbourNodes = neighbourNodes;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public boolean isNearby() {
        return isNearby;
    }

    public void setNearby(boolean nearby) {
        isNearby = nearby;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public float getRssi() {
        return rssi;
    }

    public void setRssi(float rssi) {
        this.rssi = rssi;
    }

    public static Neighbor create(String json) {
        return new Gson().fromJson(json, Neighbor.class);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static Builder builder(final String id, final String deviceName) {
        return new Builder(id, deviceName);
    }

    public static class Builder {

        private final String id;

        private final String deviceName;

        public Builder(final String id, final String deviceName) {
            this.id = id;
            this.deviceName = deviceName;
        }

        public Neighbor build() {
            return new Neighbor(this);
        }

    }
}
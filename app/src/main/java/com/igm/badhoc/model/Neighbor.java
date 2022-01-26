package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.util.List;

public class Neighbor {

    private final String deviceName;

    private final String id;

    private boolean isNearby;

    private String speed;

    private Status status;

    private String macAddress;

    private String latitude;

    private String longitude;

    private List<Neighbor> neighbourNodes;

    private Neighbor(final Builder builder) {
        this.id = builder.id;
        this.deviceName = builder.deviceName;
    }

    public String getId() {
        return id;
    }

    public String getSpeed() {
        return speed;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setPosition(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLongitude() {
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

    public void setSpeed(String speed) {
        this.speed = speed;
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
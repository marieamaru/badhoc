package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents a device with all its information
 */
public class Node {
    /**
     * Name of the device
     */
    private transient final String deviceName;
    /**
     * Unique id of the device
     */
    private transient final String id;
    /**
     * Boolean corresponding if the device is nearby or not
     */
    private transient boolean isNearby;
    /**
     * RSSI signal of the device
     */
    private transient float rssi;
    /**
     * Type of the device (smartphone...)
     */
    private final String type;
    /**
     * Speed of the device
     */
    private String speed;
    /**
     * Status of the device
     */
    private int isdominant;
    /**
     * Map of dominated devices around if the current node is dominant
     */
    private final HashMap<String, String> dominating;
    /**
     * Dominant node is the current node is dominated
     */
    private Neighbor dominant;
    /**
     * LTE signal of the device
     */
    private String lteSignal;
    /**
     * MAC address of the device
     */
    private String macAddress;
    /**
     * Latitude of the device
     */
    private String latitude;
    /**
     * Longitude of the device
     */
    private String longitude;
    /**
     * List of neighbors around the device
     */
    private final List<Neighbor> neighbours;

    private Node(final Builder builder) {
        this.id = builder.id;
        this.deviceName = builder.deviceName;
        this.isdominant = Status.DOMINATED.value;
        this.neighbours = new ArrayList<>();
        this.dominating = new HashMap<>();
        this.lteSignal = "-1";
        this.type = "1"; //smartphone
        this.speed = "0";
    }

    public String getId() {
        return id;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public int isDominant() {
        return isdominant;
    }

    public void setIsDominant(int isDominant) {
        this.isdominant = isDominant;
    }

    public Neighbor getDominant() {
        return dominant;
    }

    public void setDominant(Neighbor dominant) {
        this.dominant = dominant;
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

    public List<Neighbor> getNeighbours() {
        return neighbours;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLteSignal(String lteSignal) {
        this.lteSignal = lteSignal;
    }

    public HashMap<String, String> getDominating() {
        return dominating;
    }

    public void addToNeighborhood(Neighbor neighbor) {
        this.neighbours.add(neighbor);
    }

    public void removeFromNeighborhood(String id) {
        List<Neighbor> toRemove = new ArrayList<>();
        for (Neighbor n : this.neighbours) {
            if (n.getId().equals(id)) {
                toRemove.add(n);
            }
        }
        this.neighbours.removeAll(toRemove);
    }

    public void addToDominating(String senderId, String macAddress) {
        this.dominating.put(senderId, macAddress);
    }

    public void removeFromDominating(String senderId) {
        this.dominating.remove(senderId);

    }

    public void removeDominant() {
        this.dominant = null;
    }

    public void clearDominatingList() {
        this.dominating.clear();
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

    public float getRssi() {
        return rssi;
    }

    public void setRssi(float rssi) {
        this.rssi = rssi;
    }

    public String getLteSignal() {
        return lteSignal;
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

        public Node build() {
            return new Node(this);
        }

    }
}
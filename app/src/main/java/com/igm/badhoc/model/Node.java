package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.io.Serializable;
import java.nio.file.SecureDirectoryStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private transient final String deviceName;

    private transient final String id;

    private transient boolean isNearby;

    private transient float rssi;

    private String type;

    private String speed;

    private int isDominant;

    //if the node is dominant
    private HashMap<String, String> dominating;

    private String macAddress;

    //if the node is dominated
    private Neighbor dominant;

    private double latitude;

    private double longitude;

    private HashMap<String,Neighbor> neighborhood;

    private Node(final Builder builder) {
        this.id = builder.id;
        this.deviceName = builder.deviceName;
        this.isDominant = 0;
        this.neighborhood = new HashMap<>();
        this.dominating = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public int isDominant() {
        return isDominant;
    }

    public void setIsDominant(int isDominant) {
        this.isDominant = isDominant;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setPosition(long latitude, long longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Map<String,Neighbor> getNeighborhood() {
        return neighborhood;
    }

    public void addToNeighborhood(String id, Neighbor neighbor) {
        this.neighborhood.put(id, neighbor);
    }

    public void removeFromNeighborhood(String id){
        this.neighborhood.remove(id);
    }

    public void addToDominating(String senderId, String macAddress){
        this.dominating.put(senderId, macAddress);
    }

    public void clearDominatingList(){
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

    public static Node create(String json) {
        return new Gson().fromJson(json, Node.class);
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
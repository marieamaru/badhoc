package com.igm.badhoc.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

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

    //if the node is dominated
    private Neighbor dominant;

    private String lteSignal;

    private String macAddress;

    private String latitude;

    private String longitude;

    private List<Neighbor> neighbors;

    private Node(final Builder builder) {
        this.id = builder.id;
        this.deviceName = builder.deviceName;
        this.isDominant = 0;
        this.neighbors = new ArrayList<>();
        this.dominating = new HashMap<>();
    }

    public String getId() {
        return id;
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

    public void setType(String type) {
        this.type = type;
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

    public void setLteSignal(String lteSignal) {
        this.lteSignal = lteSignal;
    }

    public void addToNeighborhood(Neighbor neighbor) {
        this.neighbors.add(neighbor);
    }

    public void removeFromNeighborhood(String id) {
        for(Neighbor n : this.neighbors){
            if (n.getId().equals(id)){
                this.neighbors.remove(n);
            }
        }
    }

    public void addToDominating(String senderId, String macAddress) {
        this.dominating.put(senderId, macAddress);
    }

    public void removeFromDominating(String senderId) {
        this.dominating.remove(senderId);

    }

    public void removeDominant(){
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

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public String nodeKeepAliveMessage() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<HashMap<String, Object>> serializer =
                (src, typeOfSrc, context) -> {
                    if (src == null || src.isEmpty()) {
                        return null;
                    }
                    JsonArray jsonMacAddress = new JsonArray();
                    for (Map.Entry<String, Object> entry : src.entrySet()) {
                        jsonMacAddress.add("" + entry.getValue());
                    }
                    return jsonMacAddress;
                };
        JsonSerializer<Neighbor> serializer2 =
                (src, typeOfSrc, context) -> {
                    if (src == null) {
                        return null;
                    }
                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("macAddress", src.getMacAddress());
                    jsonObject.addProperty("RSSI", src.getRSSI());

                    return jsonObject;
                };
        gsonBuilder.registerTypeAdapter(HashMap.class, serializer);
        gsonBuilder.registerTypeAdapter(Neighbor.class, serializer2);
        return gsonBuilder.create().toJson(this);
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
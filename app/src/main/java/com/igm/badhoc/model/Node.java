package com.igm.badhoc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Node {

    private final String id;

    private final String manufacturer;

    private final String model;

    private final String speed;

    private Status status;

    private String macAddress;

    private String latitude;

    private String longitude;

    private List<Node> neighbourNodes;

    public Node(final Builder builder) {
        this.id = builder.id;
        this.model = builder.model;
        this.manufacturer = builder.manufacturer;
        this.speed = builder.speed;
        this.status = builder.status;
        this.macAddress = builder.macAddress;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.neighbourNodes = builder.neighbourNodes;
    }

    public String getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getManufacturer() {
        return manufacturer;
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

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public List<Node> getNeighbourNodes() {
        return neighbourNodes;
    }

    public void setNeighbourNodes(List<Node> neighbourNodes) {
        this.neighbourNodes = neighbourNodes;
    }

    public static class Builder {

        private final String id;

        private final String model;

        private final String manufacturer;

        private final  String speed;

        private Status status;

        private String macAddress;

        private String latitude;

        private String longitude;

        private List<Node> neighbourNodes;

        public Builder(final String model, final String manufacturer, final String speed, final Status status, final String macAddress, final String latitude, final String longitude) {
            this.id = UUID.randomUUID().toString();
            this.model = model;
            this.manufacturer = manufacturer;
            this.speed = speed;
            this.status = status;
            this.macAddress = macAddress;
            this.latitude = latitude;
            this.longitude = longitude;
            this.neighbourNodes = new ArrayList<>();
        }

        public Node build() {
            return new Node(this);
        }

    }
}

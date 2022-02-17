package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.io.Serializable;

public class MessageBadhoc implements Serializable {

    public final static int INCOMING_MESSAGE = 0;
    public final static int OUTGOING_MESSAGE = 1;

    private int direction;
    private String deviceName;
    private final String text;

    public MessageBadhoc(String text) {
        this.text = text;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getText() {
        return text;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

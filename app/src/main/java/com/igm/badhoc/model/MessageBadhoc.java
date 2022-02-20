package com.igm.badhoc.model;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Class that represents a message in Badhoc
 */
public class MessageBadhoc implements Serializable {
    /**
     * Tag corresponding to an incoming message
     */
    public final static int INCOMING_MESSAGE = 0;
    /**
     * Tag corresponding to an outgoing message
     */
    public final static int OUTGOING_MESSAGE = 1;
    /**
     * The direction of the message : incoming or outgoing
     */
    private int direction;
    /**
     * Name of the device
     */
    private String deviceName;
    /**
     * Content of the message
     */
    private final String text;

    /**
     * Constructor for the MessageBadhoc class
     *
     * @param text content of the message
     */
    public MessageBadhoc(String text) {
        this.text = text;
    }

    /**
     * Getter for the direction field
     *
     * @return the direction of the message
     */
    public int getDirection() {
        return direction;
    }

    /**
     * Setter for the direction field
     *
     * @param direction the direction of the message
     */
    public void setDirection(int direction) {
        this.direction = direction;
    }

    /**
     * Getter for the device name field
     *
     * @return the name of the device
     */
    public String getDeviceName() {
        return deviceName;
    }

    /**
     * Getter for the content field
     *
     * @return the content of the message
     */
    public String getText() {
        return text;
    }

    /**
     * Setter for the device name field
     *
     * @param deviceName name of the device
     */
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}

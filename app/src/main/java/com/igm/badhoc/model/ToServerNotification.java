package com.igm.badhoc.model;

public class ToServerNotification {
    private final String macAddress;
    private final String message;

    public ToServerNotification(final String macAddress, final String message) {
        this.macAddress = macAddress;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getMacAddress() {
        return macAddress;
    }
}


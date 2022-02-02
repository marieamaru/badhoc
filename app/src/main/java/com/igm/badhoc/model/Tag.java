package com.igm.badhoc.model;

public enum Tag {

    BROADCAST_CHAT("Broadcast"),
    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    MESSAGES("messages");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}
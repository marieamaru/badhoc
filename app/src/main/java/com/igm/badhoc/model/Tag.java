package com.igm.badhoc.model;

public enum Tag {

    BROADCAST_CHAT("Broadcast"),
    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    PAYLOAD_RSSI("rssi"),
    PAYLOAD_IS_DOMINANT("status");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}

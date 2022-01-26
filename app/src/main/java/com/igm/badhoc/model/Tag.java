package com.igm.badhoc.model;

public enum Tag {

    INTENT_EXTRA_NAME("peerName"),
    INTENT_EXTRA_UUID("peerUuid"),
    INTENT_EXTRA_TYPE("deviceType"),
    INTENT_EXTRA_MSG("message"),
    BROADCAST_CHAT("Broadcast"),
    PAYLOAD_DEVICE_TYPE("device_type"),
    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    MESSAGES("messages");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}

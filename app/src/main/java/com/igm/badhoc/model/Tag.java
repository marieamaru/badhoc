package com.igm.badhoc.model;

public enum Tag {

    BROADCAST_CHAT("Broadcast"),
    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    PAYLOAD_RSSI("rssi"),
    PAYLOAD_IS_DOMINANT("status"),
    INTENT_SERVER_SERVICE("mqtt"),
    INTENT_MAIN_ACTIVITY("server"),
    ACTION_CONNECT("action_connect"),
    ACTION_UPDATE_NODE_INFO("action_update_node_info");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}

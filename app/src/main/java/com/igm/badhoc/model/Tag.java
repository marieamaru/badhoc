package com.igm.badhoc.model;

public enum Tag {

    BROADCAST_CHAT("Broadcast"),
    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    PAYLOAD_RSSI("rssi"),
    PAYLOAD_IS_DOMINANT("status"),
    PAYLOAD_BROADCAST_TYPE("broadcast_type"),
    PAYLOAD_REGULAR_BROADCAST("regular"),
    PAYLOAD_FROM_SERVER("from_server"),
    PAYLOAD_NO_LONGER_DOMINANT("no_longer_dominant"),
    PAYLOAD_DOMINANT("payload_dominant"),

    INTENT_SERVER_SERVICE("mqtt"),
    INTENT_MAIN_ACTIVITY("server"),

    ACTION_CONNECT("action_connect"),
    ACTION_MESSAGE_RECEIVED("message_received_from_topic"),
    ACTION_UPDATE_NODE_INFO("action_update_node_info"),
    ACTION_CHANGE_TITLE("change_title");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}

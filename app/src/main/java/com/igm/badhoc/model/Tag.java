package com.igm.badhoc.model;

/**
 * Enum representing the different strings associated to a key for payload content, or intent use
 */
public enum Tag {

    BROADCAST_CHAT("broadcast"),
    PRIVATE_CHAT("private_chat"),

    PAYLOAD_DEVICE_NAME("device_name"),
    PAYLOAD_MAC_ADDRESS("device_mac_address"),
    PAYLOAD_TEXT("text"),
    PAYLOAD_RSSI("rssi"),
    PAYLOAD_IMAGE("image"),
    PAYLOAD_MESSAGE_TO_SERVER("message_to_server"),
    PAYLOAD_IS_DOMINANT("status"),
    PAYLOAD_BROADCAST_TYPE("broadcast_type"),
    PAYLOAD_PRIVATE_TYPE("private_type"),
    PAYLOAD_REGULAR_BROADCAST("regular"),
    PAYLOAD_FROM_SERVER("from_server"),
    PAYLOAD_NO_LONGER_DOMINANT("no_longer_dominant"),
    PAYLOAD_POTENTIAL_DOMINANT("payload_potential_dominant"),

    INTENT_SERVER_SERVICE("mqtt"),
    INTENT_MAIN_ACTIVITY("main_activity"),
    INTENT_MSG_PROGRESS("message_progress"),

    ACTION_CONNECT("action_connect"),
    ACTION_NOTIFICATION_RECEIVED("message_received_from_topic"),
    ACTION_UPDATE_NODE_INFO("action_update_node_info"),
    ACTION_CHANGE_TITLE("change_title"),
    ACTION_SEND_MESSAGE_TO_SERVER("send_message_to_server"),

    TOPIC_NOTIFS("notifs"),
    TOPIC_KEEP_ALIVE("nodekeepalive"),
    TOPIC_TO_SERVER("nodekeepalive"),
    TITLE_NOT_DOMINANT("Notifications from dominant"),
    TITLE_DOMINANT("Notifications from server");

    public final String value;

    Tag(String value) {
        this.value = value;
    }
}

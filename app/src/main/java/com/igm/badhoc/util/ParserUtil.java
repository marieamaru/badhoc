package com.igm.badhoc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonSerializer;
import com.igm.badhoc.model.FromServerNotification;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.ToServerNotification;

import java.util.HashMap;
import java.util.Map;

public class ParserUtil {

    /**
     * Serializing method to form a message of correct format for the nodekeepalive topic
     *
     * @param node node to get information from
     * @return the message to send to the server
     */
    public static String parseNodeKeepAliveMessage(final Node node) {
        GsonBuilder gsonBuilder = new GsonBuilder();

        JsonSerializer<HashMap<String, Object>> serializer =
                (src, typeOfSrc, context) -> {
                    if (src.isEmpty() && node.isDominant() == 0) {
                        return null;
                    }
                    JsonArray jsonMacAddress = new JsonArray();
                    for (Map.Entry<String, Object> entry : src.entrySet()) {
                        jsonMacAddress.add("" + entry.getValue());
                    }
                    return jsonMacAddress;
                };
        gsonBuilder.registerTypeAdapter(HashMap.class, serializer);
        return gsonBuilder.create().toJson(node);
    }

    /**
     * Parser method to form a message of correct format from the notifs topic
     *
     * @param notification notification to parse
     * @return the message sent by the server
     */
    public static String parseTopicNotifsResponse(final String notification) {
        Gson gson = new Gson();
        FromServerNotification fromServerNotification = gson.fromJson(notification, FromServerNotification.class);
        return fromServerNotification.getNotif();
    }

    /**
     * Serializing method to form a message for the server
     *
     * @param toServerNotification message to parse
     * @return the message sent to the server
     */
    public static String parseMessageForServer(final ToServerNotification toServerNotification) {
        Gson gson = new Gson();
        return gson.toJson(toServerNotification);
    }
}

package com.igm.badhoc.model;

import java.util.Calendar;

/**
 * Class that represents a notification from the server
 */
public class Notification {
    /**
     * Date the notification was received
     */
    private final String date;
    /**
     * Content of the notification
     */
    private final String text;

    public Notification(final String text) {
        this.date = Calendar.getInstance().getTime().toString();
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}

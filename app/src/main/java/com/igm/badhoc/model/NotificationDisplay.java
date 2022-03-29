package com.igm.badhoc.model;

import java.util.Calendar;

/**
 * Class that represents a notification from the server
 */
public class NotificationDisplay {
    /**
     * Tag corresponding to an incoming message
     */
    public final static int INCOMING_MESSAGE = 0;
    /**
     * Tag corresponding to an outgoing message
     */
    public final static int OUTGOING_MESSAGE = 1;
    /**
     * Date the notification was received
     */
    private final String date;
    /**
     * Content of the notification
     */
    private final String text;
    /**
     * Direction of the notification
     */
    private int direction;

    public NotificationDisplay(final String text) {
        this.date = Calendar.getInstance().getTime().toString();
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }
}

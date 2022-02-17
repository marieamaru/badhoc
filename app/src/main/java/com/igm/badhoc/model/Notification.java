package com.igm.badhoc.model;

import java.util.Calendar;

public class Notification {

    private final String date;
    private final String text;

    public Notification(final String text) {
        Calendar cal = Calendar.getInstance();
        cal.clear(Calendar.ZONE_OFFSET);
        this.date = cal.getTime().toString();
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}

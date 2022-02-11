package com.igm.badhoc.model;

public class Notification {

    private final String date;
    private final String text;

    public Notification(String date, String text) {
        this.date = date;
        this.text = text;
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}

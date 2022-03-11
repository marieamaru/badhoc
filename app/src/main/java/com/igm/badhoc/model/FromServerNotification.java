package com.igm.badhoc.model;

public class FromServerNotification {
    private final String dominant;
    private final String notif;

    public FromServerNotification(final String dominant, final String notif) {
        this.dominant = dominant;
        this.notif = notif;
    }

    public String getDominant() {
        return dominant;
    }

    public String getNotif() {
        return notif;
    }
}

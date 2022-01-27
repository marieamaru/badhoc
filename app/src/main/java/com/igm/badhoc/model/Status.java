package com.igm.badhoc.model;

public enum Status {
    UNDEFINED(-1), DOMINATED(1), DOMINATING(0);

    public int value;

    Status(int i) {
        this.value = i;
    }

}

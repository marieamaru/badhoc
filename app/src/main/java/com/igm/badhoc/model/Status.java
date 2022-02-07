package com.igm.badhoc.model;

public enum Status {
    DOMINATED(0), DOMINATING(1);

    public int value;

    Status(int i) {
        this.value = i;
    }

}

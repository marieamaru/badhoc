package com.igm.badhoc.model;

/**
 * Enum representing the status of the device
 */
public enum Status {
    /**
     * if the device is dominated
     */
    DOMINATED(0),
    /**
     * if the device is dominating
     */
    DOMINATING(1);

    public int value;

    Status(int i) {
        this.value = i;
    }

}

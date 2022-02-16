package com.igm.badhoc.model;

import com.google.gson.Gson;
import com.google.gson.annotations.JsonAdapter;
import com.igm.badhoc.serializer.NeighborDominatingAdapter;

import java.util.Objects;

@JsonAdapter(NeighborDominatingAdapter.class)
public class Neighbor {
    private final String id;
    private final String macAddress;
    private final float RSSI;

    public Neighbor(final String id, final String macAddress, final float RSSI) {
        this.id = id;
        this.macAddress = macAddress;
        this.RSSI = RSSI;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public float getRSSI() {
        return RSSI;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbor neighbor = (Neighbor) o;
        return Float.compare(neighbor.RSSI, RSSI) == 0 && Objects.equals(macAddress, neighbor.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress, RSSI);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }


}

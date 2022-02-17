package com.igm.badhoc.serializer;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.igm.badhoc.model.Neighbor;

import java.io.IOException;

public class NeighborDominantAdapter extends TypeAdapter<Neighbor> {
    @Override
    public void write(JsonWriter writer, Neighbor src) throws IOException {
        writer.beginObject();
        writer.name("macAddress");
        writer.value(src.getMacAddress());
        writer.endObject();
    }

    @Override
    public Neighbor read(JsonReader in) throws IOException {
        return null;
    }
}
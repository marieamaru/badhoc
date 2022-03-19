package com.igm.badhoc.serializer;
import static org.assertj.core.api.Assertions.assertThat;
import com.igm.badhoc.model.Neighbor;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class NeighborDominatingSerializerTest {
    @Test
    @DisplayName("Test writer")
    public void write() {
        Neighbor neighbor = new Neighbor("id", "00:00:00:00:00:01", -2);
        final String expectedString = "{\"macAddress\":\"00:00:00:00:00:01\",\"RSSI\":-2.0}";
        assertThat(neighbor.toString()).isEqualTo(expectedString);
    }
}

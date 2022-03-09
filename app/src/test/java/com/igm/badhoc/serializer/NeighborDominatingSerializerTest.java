package com.igm.badhoc.serializer;
import static org.assertj.core.api.Assertions.assertThat;
import com.igm.badhoc.model.Neighbor;

import org.junit.Test;

public class NeighborDominatingSerializerTest {
    @Test
    public void write() {
        Neighbor neighbor = new Neighbor("id", "00:00:00:00:00:01", -2);
        final String expectedString = "{\"macAddress\":\"00:00:00:00:00:01\",\"RSSI\":-2.0}";
        assertThat(neighbor.toString()).isEqualTo(expectedString);
    }
}

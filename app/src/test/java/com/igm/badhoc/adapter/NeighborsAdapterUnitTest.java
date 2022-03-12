package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class NeighborsAdapterUnitTest {

    @Test
    @DisplayName("Test for constructor")
    public void test(){
        final NeighborsAdapter neighborsAdapter = new NeighborsAdapter();
        assertThat(neighborsAdapter).isNotNull();
        assertThat(neighborsAdapter.getItemCount()).isEqualTo(0);
    }
}

package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NeighborsAdapterUnitTest {

    @Test
    public void test(){
        final NeighborsAdapter neighborsAdapter = new NeighborsAdapter();
        assertThat(neighborsAdapter).isNotNull();
        assertThat(neighborsAdapter.getItemCount()).isEqualTo(0);
    }
}

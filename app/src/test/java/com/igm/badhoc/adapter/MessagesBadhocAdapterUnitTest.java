package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class MessagesBadhocAdapterUnitTest {
    @Test
    public void test() {
        final String conversationId = "123";
        final String content = "test";
        final MessagesBadhocAdapter messagesBadhocAdapter = new MessagesBadhocAdapter(conversationId);
        assertThat(messagesBadhocAdapter).isNotNull();
        assertThat(messagesBadhocAdapter.getItemCount()).isEqualTo(0);
    }


}

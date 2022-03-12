package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class MessagesBadhocAdapterUnitTest {
    @Test
    @DisplayName("Test for constructor")
    public void test() {
        final String conversationId = "123";
        final MessagesBadhocAdapter messagesBadhocAdapter = new MessagesBadhocAdapter(conversationId);
        assertThat(messagesBadhocAdapter).isNotNull();
        assertThat(messagesBadhocAdapter.getItemCount()).isEqualTo(0);
    }


}

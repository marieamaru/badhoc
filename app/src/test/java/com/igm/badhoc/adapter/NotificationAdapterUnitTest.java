package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class NotificationAdapterUnitTest {
    @Test
    @DisplayName("Test for constructor")
    public void test(){
        final NotificationAdapter notificationAdapter = new NotificationAdapter();
        assertThat(notificationAdapter).isNotNull();
        assertThat(notificationAdapter.getItemCount()).isEqualTo(0);
    }
}

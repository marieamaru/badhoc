package com.igm.badhoc.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class NotificationAdapterUnitTest {
    @Test
    public void test(){
        final NotificationAdapter notificationAdapter = new NotificationAdapter();
        assertThat(notificationAdapter).isNotNull();
        assertThat(notificationAdapter.getItemCount()).isEqualTo(0);
    }
}

package com.igm.badhoc.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class FromServerNotificationUnitTest {

    @Test
    @DisplayName("Test for constructor and getters")
    public void test() {
        final String dominant = "00:00:00:00:00:00";
        final String message = "test";
        final FromServerNotification fromServerNotification = new FromServerNotification(dominant, message);
        assertThat(fromServerNotification).isNotNull();
        assertThat(fromServerNotification.getDominant()).isEqualTo(dominant);
        assertThat(fromServerNotification.getNotif()).isEqualTo(message);
    }
}

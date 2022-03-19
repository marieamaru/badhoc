package com.igm.badhoc.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class NotificationDisplayTest {
    @Test
    @DisplayName("Test for constructor")
    public void test() {
        final String message = "test";
        final NotificationDisplay notificationDisplay = new NotificationDisplay(message);
        assertThat(notificationDisplay).isNotNull();
        assertThat(notificationDisplay.getDate()).isNotEmpty();
        assertThat(notificationDisplay.getText()).isEqualTo(message);
    }
}

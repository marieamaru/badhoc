package com.igm.badhoc.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class MessageBadhocUnitTest {
    @Test
    @DisplayName("Test for constructor and getters")
    public void test() {
        final String text = "test";
        final String deviceName = "deviceTest";
        final MessageBadhoc messageBadhoc = new MessageBadhoc(text);
        messageBadhoc.setDeviceName(deviceName);
        messageBadhoc.setDirection(MessageBadhoc.INCOMING_MESSAGE);
        assertThat(messageBadhoc).isNotNull();
        assertThat(messageBadhoc.getText()).isEqualTo(text);
        assertThat(messageBadhoc.getDeviceName()).isEqualTo(deviceName);
        assertThat(messageBadhoc.getDirection()).isEqualTo(MessageBadhoc.INCOMING_MESSAGE);
    }
}

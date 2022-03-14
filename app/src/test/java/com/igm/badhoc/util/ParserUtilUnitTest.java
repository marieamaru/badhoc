package com.igm.badhoc.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;
import com.igm.badhoc.model.ToServerNotification;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class ParserUtilUnitTest {

    @Test
    @DisplayName("Test for empty dominating list in node object")
    public void nodeKeepAliveMessageTestNoDominating() {
        final Node node = Node.builder("123", "testDevice").build();
        node.setIsDominant(1);
        node.setMacAddress("00:00:00:00:00:00");
        node.setRssi(-1);
        final String nodeKeepAliveMessage = ParserUtil.parseNodeKeepAliveMessage(node);
        final String expectedMessage = "{\"type\":\"1\",\"speed\":\"0\",\"isdominant\":1,\"dominating\":[],\"lteSignal\":\"-1\",\"macAddress\":\"00:00:00:00:00:00\",\"neighbours\":[]}";
        assertNotNull(node);
        assertThat(nodeKeepAliveMessage).isNotEmpty();
        assertThat(nodeKeepAliveMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Test for not empty dominating list in node object")
    public void nodeKeepAliveMessageTestWithDominating() {
        final Node node = Node.builder("123", "testDevice").build();
        node.setIsDominant(1);
        node.setMacAddress("00:00:00:00:00:00");
        node.setRssi(-1);
        node.addToDominating("1", "00:00:00:00:00:01");
        node.addToNeighborhood(new Neighbor("1", "00:00:00:00:00:01", -2));
        final String nodeKeepAliveMessage = ParserUtil.parseNodeKeepAliveMessage(node);
        final String expectedMessage = "{\"type\":\"1\",\"speed\":\"0\",\"isdominant\":1,\"dominating\":[\"00:00:00:00:00:01\"],\"lteSignal\":\"-1\",\"macAddress\":\"00:00:00:00:00:00\",\"neighbours\":[{\"macAddress\":\"00:00:00:00:00:01\",\"RSSI\":-2.0}]}";
        assertNotNull(node);
        assertThat(nodeKeepAliveMessage).isNotEmpty();
        assertThat(nodeKeepAliveMessage).isEqualTo(expectedMessage);
    }

    @Test
    @DisplayName("Test for parser of notifs topic response")
    public void parseTopicNotifsResponseTest() {
        final String topicResponse = "{\"dominant\":\"00:00:00:00:00:00\",\"notif\":\"Spectacle de magie demain.\"}";
        final String parsedTopicResponse = ParserUtil.parseTopicNotifsResponse(topicResponse);
        final String expectedParsedTopicResponse = "Spectacle de magie demain.";
        assertThat(parsedTopicResponse).isNotBlank();
        assertThat(parsedTopicResponse).isNotEmpty();
        assertThat(parsedTopicResponse).isEqualTo(expectedParsedTopicResponse);
    }

    @Test
    @DisplayName("Test for parser of sending topic message")
    public void parseTopicServerMessageTest(){
        final String message = "Ceci est un test";
        final String address = "00:00:00:00:00:00";
        final ToServerNotification toServerNotification = new ToServerNotification(address, message);
        final String parsedTopicMessage = ParserUtil.parseMessageForServer(toServerNotification);
        final String expectedParsedTopicMessage = "{\"macAddress\":\"00:00:00:00:00:00\",\"message\":\"Ceci est un test\"}";
        assertThat(parsedTopicMessage).isNotBlank();
        assertThat(parsedTopicMessage).isNotEmpty();
        assertThat(parsedTopicMessage).isEqualTo(expectedParsedTopicMessage);
    }
}

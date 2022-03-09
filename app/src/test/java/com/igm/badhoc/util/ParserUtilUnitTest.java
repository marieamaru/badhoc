package com.igm.badhoc.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.igm.badhoc.model.Neighbor;
import com.igm.badhoc.model.Node;

import org.junit.Test;

public class ParserUtilUnitTest {

    @Test
    public void nodeKeepAliveMessageTestNoDominating() {
        final Node node = Node.builder("123", "testDevice").build();
        node.setIsDominant(1);
        node.setMacAddress("00:00:00:00:00:00");
        node.setRssi(-1);
        final String nodeKeepAliveMessage = ParserUtil.parseNodeKeepAliveMessage(node);
        final String expectedMessage = "{\"type\":\"1\",\"speed\":\"0\",\"isdominant\":1,\"dominating\":[],\"lteSignal\":\"-1\",\"macAddress\":\"00:00:00:00:00:00\",\"latitude\":\"0.0\",\"longitude\":\"0.0\",\"neighbours\":[]}";
        assertNotNull(node);
        assertThat(nodeKeepAliveMessage).isNotEmpty();
        assertThat(nodeKeepAliveMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void nodeKeepAliveMessageTestWithDominating() {
        final Node node = Node.builder("123", "testDevice").build();
        node.setIsDominant(1);
        node.setMacAddress("00:00:00:00:00:00");
        node.setRssi(-1);
        node.addToDominating("1", "00:00:00:00:00:01");
        node.addToNeighborhood(new Neighbor("1", "00:00:00:00:00:01", -2));
        final String nodeKeepAliveMessage = ParserUtil.parseNodeKeepAliveMessage(node);
        final String expectedMessage = "{\"type\":\"1\",\"speed\":\"0\",\"isdominant\":1,\"dominating\":[\"00:00:00:00:00:01\"],\"lteSignal\":\"-1\",\"macAddress\":\"00:00:00:00:00:00\",\"latitude\":\"0.0\",\"longitude\":\"0.0\",\"neighbours\":[{\"macAddress\":\"00:00:00:00:00:01\",\"RSSI\":-2.0}]}";
        assertNotNull(node);
        assertThat(nodeKeepAliveMessage).isNotEmpty();
        assertThat(nodeKeepAliveMessage).isEqualTo(expectedMessage);
    }

    @Test
    public void parseTopicNotifsResponseTest() {
        final String topicResponse = "{\"dominant\":\"00:00:00:00:00:00\",\"notif\":\"Spectacle de magie demain.\"}";
        final String parsedTopicResponse = ParserUtil.parseTopicNotifsResponse(topicResponse);
        final String expectedParsedTopicResponse = "Spectacle de magie demain.";
        assertThat(parsedTopicResponse).isNotBlank();
        assertThat(parsedTopicResponse).isNotEmpty();
        assertThat(parsedTopicResponse).isEqualTo(expectedParsedTopicResponse);
    }
}

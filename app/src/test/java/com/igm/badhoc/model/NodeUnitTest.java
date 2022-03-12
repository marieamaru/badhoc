package com.igm.badhoc.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;

public class NodeUnitTest {

    @Test
    @DisplayName("Test for constructor")
    public void createNodeTest() {
        final Node node = Node.builder("123", "testDevice").build();
        node.setMacAddress("00:00:00:00:00:00");
        node.setRssi(-1);
        node.setPosition("1", "1");
        assertNotNull(node);
        assertThat(node.getId()).isEqualTo("123");
        assertThat(node.getDeviceName()).isEqualTo("testDevice");
        assertThat(node.getMacAddress()).isNotEmpty();
        assertThat(node.getLteSignal()).isEqualTo("-1");
        assertThat(node.getLatitude()).isEqualTo("1");
        assertThat(node.getLongitude()).isEqualTo("1");
        assertThat(node.getNeighbours()).isEqualTo(Collections.EMPTY_LIST);
        assertThat(node.getDominant()).isNull();
    }

    @Test
    @DisplayName("Test add to dominating list")
    public void addToDominatingTest(){
        final Node node = Node.builder("123", "testDevice").build();
        assertThat(node.getDominating()).isEmpty();
        node.addToDominating("1", "00:00:00:00:00:01");
        assertThat(node.getDominating()).hasSize(1);
    }

    @Test
    @DisplayName("Test remove from dominating list")
    public void removeFromDominatingTest(){
        final Node node = Node.builder("123", "testDevice").build();
        node.addToDominating("1", "00:00:00:00:00:01");
        node.removeFromDominating("1");
        assertThat(node.getDominating()).isEmpty();
    }

    @Test
    @DisplayName("Test clear dominating list")
    public void clearDominatingTest(){
        final Node node = Node.builder("123", "testDevice").build();
        node.addToDominating("1", "00:00:00:00:00:01");
        node.clearDominatingList();
        assertThat(node.getDominating()).isEmpty();
    }

    @Test
    @DisplayName("Test setter for dominant field")
    public void setDominantTest(){
        final Node node = Node.builder("123", "testDevice").build();
        final Neighbor dominant = new Neighbor("1", "00:00:00:00:00:01", -2);
        node.setDominant(dominant);
        assertThat(node.getDominant()).isNotNull();
        assertThat(node.getDominant()).isEqualTo(dominant);
    }

    @Test
    @DisplayName("Test remove for dominant field")
    public void removeDominantTest(){
        final Node node = Node.builder("123", "testDevice").build();
        final Neighbor dominant = new Neighbor("1", "00:00:00:00:00:01", -2);
        node.setDominant(dominant);
        node.removeDominant();
        assertThat(node.getDominant()).isNull();
    }

}

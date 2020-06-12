package com.main.Networking.requests;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

public class GameRequestTest {

    GameRequest gameRequest;

    @Before
    public void setUp() {
        gameRequest = new GameRequest();
        Vector<String> message = new Vector<>();
        message.add("test");
        gameRequest.setMessage(message);
    }

    @Test
    public void appendMessage() {
        gameRequest.appendMessage("appending message");

        Assert.assertEquals(2, gameRequest.getMessage().size());
        Assert.assertEquals("appending message", gameRequest.getMessage().elementAt(1));
    }

    @Test
    public void clearMessage() {
        gameRequest.clearMessage();

        Assert.assertTrue(gameRequest.getMessage().isEmpty());
    }
}
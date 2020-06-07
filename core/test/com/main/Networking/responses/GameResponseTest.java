package com.main.Networking.responses;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

public class GameResponseTest {

    GameResponse gameResponse;

    @Before
    public void setUp() {
        gameResponse = new GameResponse();
        Vector<String> message = new Vector<>();
        message.add("test");
        gameResponse.setMessage(message);
    }

    @Test
    public void appendMessage() {
        gameResponse.appendMessage("appending message");

        Assert.assertEquals(2, gameResponse.getMessage().size());
        Assert.assertEquals("appending message", gameResponse.getMessage().elementAt(1));
    }

    @Test
    public void clearMessage() {
        gameResponse.clearMessage();

        Assert.assertTrue(gameResponse.getMessage().isEmpty());
    }
}
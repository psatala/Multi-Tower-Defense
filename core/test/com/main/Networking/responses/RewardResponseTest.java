package com.main.Networking.responses;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;


public class RewardResponseTest {

    RewardResponse rewardResponse;

    @Before
    public void setUp() {
        rewardResponse = new RewardResponse();
        Vector<String> message = new Vector<>();
        message.add("test");
        rewardResponse.setMessage(message);
    }

    @Test
    public void appendMessage() {
        rewardResponse.appendMessage("appending message");

        Assert.assertEquals(2, rewardResponse.getMessage().size());
        Assert.assertEquals("appending message", rewardResponse.getMessage().elementAt(1));
    }

    @Test
    public void clearMessage() {
        rewardResponse.clearMessage();

        Assert.assertTrue(rewardResponse.getMessage().isEmpty());
    }
}
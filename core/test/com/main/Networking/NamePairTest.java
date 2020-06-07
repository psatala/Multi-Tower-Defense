package com.main.Networking;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NamePairTest {


    @Test
    public void getKey() {
        NamePair namePair = new NamePair(12, "host");

        Assert.assertEquals(12, namePair.getKey().intValue());
    }

    @Test
    public void getValue() {
        NamePair namePair = new NamePair();
        namePair.setId(13);
        namePair.setName("host2");

        Assert.assertEquals("host2", namePair.getValue());
    }
}
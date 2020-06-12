package com.main;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class InfoActorTest {
    InfoActor a;

    @Before
    public void beforeEach() {
        a = new InfoActor(1000, 500, 0);
    }

    @Test
    public void testCoins() {
        Assert.assertTrue(a.spendCoins(2000));
        Assert.assertFalse(a.spendCoins(1));
        a.addCoins(1000);
        a.addCoins(1);
        Assert.assertFalse(a.spendCoins(2000));
        Assert.assertTrue(a.spendCoins(1));
    }
}

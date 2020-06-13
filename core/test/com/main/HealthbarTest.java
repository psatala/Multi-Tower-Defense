package com.main;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HealthbarTest {
    Healthbar a;

    @Before
    public void beforeEach() {
        a = new Healthbar(100, 100, false);
    }


    @Test
    public void testHealthbarConstructor() {
        Assert.assertEquals(100, a.getHP(), 0.0001);
    }


    @Test
    public void testDamage() {
        boolean x = a.damage(60);
        Assert.assertFalse(x);
        Assert.assertEquals(40, a.getHP(), 0.0001);
        x = a.damage(20);
        Assert.assertFalse(x);
        Assert.assertEquals(20, a.getHP(), 0.0001);
        x = a.damage(40);
        Assert.assertTrue(x);
        Assert.assertEquals(0, a.getHP(), 0.0001);
        x = a.damage(40);
        Assert.assertTrue(x);
        Assert.assertEquals(0, a.getHP(), 0.0001);
    }
}

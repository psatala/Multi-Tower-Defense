package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class MissileTest {
    Missile a;
    Missile b;
    static Entity e1;
    static Entity e2;


    @BeforeClass
    static public void before() {
        e1 = new Entity("mainTower", 0, false);
        e1.setPosition(20, 30, Align.center);
        e2 = new Entity("mainTower", 1, false);
        e2.setPosition(90, 70, Align.center);
    }

    @Before
    public void beforeEach() {
        a = new Missile(e2, e1, "missile", false);
    }


    @Test
    public void testMissileConstructor() {
        b = new Missile("M 12 missile 0 10.0 12.0 100.0 20.0 50.0", false);
        Assert.assertEquals(12, b.getId());
        Assert.assertEquals(0, b.getPlayerId());
        Assert.assertEquals(10, b.getX(Align.center), 0.0001);
        Assert.assertEquals(12, b.getY(Align.center), 0.0001);
        Assert.assertEquals("missile", b.getType());
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
            b.act(0.1f);
        }
        Assert.assertEquals(100, b.getX(Align.center), 0.001);
        Assert.assertEquals(20, b.getY(Align.center), 0.001);
        Assert.assertEquals(90, a.getX(Align.center), 0.001);
        Assert.assertEquals(70, a.getY(Align.center), 0.001);
    }

    @Test
    public void testToString() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        String x = a.toString();
        int id = a.getId();
        Assert.assertEquals("M "+id+" missile 0 10.0 12.0 90.0 70.0 20.0", x.trim());
    }


    @Test
    public void testSetState() {
        a.setState("M 12 missile 0 10.0 12.0 60.0 50.0 40.0");
        Assert.assertEquals(10, a.getX(Align.center), 0.0001);
        Assert.assertEquals(12, a.getY(Align.center), 0.0001);
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
        }
        //Target and damage should be unchangeable
        Assert.assertEquals(90, a.getX(Align.center), 0.001);
        Assert.assertEquals(70, a.getY(Align.center), 0.001);
        Assert.assertEquals(20, a.getDamage(), 0.001);
    }


    @Test
    public void testHitObject() {
        Assert.assertFalse(a.hitObject(e2));
        a.act(0.1f);
        Assert.assertFalse(a.hitObject(e2));
        Assert.assertTrue(a.isAlive());
        for(int i = 0; i < 20; ++i) {
            a.act(0.1f);
        }
        Assert.assertTrue(a.hitObject(e2));
        Assert.assertFalse(a.isAlive());
    }


    @Test
    public void testGetPosition() {
        Vector3 x = a.getPosition(Align.center);
        Vector3 y = a.getPosition(Align.bottomLeft);
        Assert.assertEquals(20, x.x, 0.0001);
        Assert.assertEquals(30, x.y, 0.0001);
        Assert.assertEquals(16, y.x, 0.0001);
        Assert.assertEquals(26, y.y, 0.0001);
    }
}

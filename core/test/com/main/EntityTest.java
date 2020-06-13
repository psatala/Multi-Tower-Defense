package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EntityTest {
    Entity a;
    Entity b;
    Entity c;

    @Before
    public void beforeEach() {
        a = new Entity("firstTower", 0, false);
        b = new Entity("firstTower", 0, false);
    }

    @Test
    public void testEntityConstructors() {
        c = new Entity("E 12 mainTower 0 10.0 12.0 2.0 100.0", false);
        Assert.assertEquals(1, b.getId() - a.getId());
        Assert.assertEquals(12, c.getId());
        Assert.assertEquals(0, c.getPlayerId());
        Assert.assertEquals(10, c.getX(Align.center), 0.0001);
        Assert.assertEquals(12, c.getY(Align.center), 0.0001);
        Assert.assertEquals(2, c.getReloadTime(), 0.001);
        Assert.assertEquals(100, c.getHP(), 0.0001);
        Assert.assertEquals("mainTower", c.getType());
    }

    @Test
    public void testSetState() {
        a.setState("E 12 mainTower 0 10.0 12.0 2.0 100.0");
        Assert.assertEquals(10, a.getX(Align.center), 0.0001);
        Assert.assertEquals(12, a.getY(Align.center), 0.0001);
        Assert.assertEquals(2, a.getReloadTime(), 0.001);
        Assert.assertEquals(100, a.getHP(), 0.0001);
    }

    @Test
    public void testToString() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        String x = a.toString();
        int id = a.getId();
        Assert.assertEquals("E "+id+" firstTower 0 10.0 12.0 1.0 120.0", x.trim());
    }

    @Test
    public void testGridUpdate() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        Vector3 x = a.gridUpdate();
        Assert.assertEquals(10, x.x, 0.0001);
        Assert.assertEquals(12, x.y, 0.0001);
        Assert.assertEquals(0, x.z, 0.0001);
    }


    @Test
    public void testDamage() {
        float x = a.getHP();
        a.damage(20);
        float y = a.getHP();
        Assert.assertEquals(20, x-y, 0.0001);
    }


    @Test
    public void testUpdate() {
        float x = a.getReloadTime();
        a.update(0.5f);
        float y = a.getReloadTime();
        Assert.assertEquals(0.5, x-y, 0.0001);
    }

    @Test
    public void testShoot() {
        boolean t = a.shoot();
        Assert.assertFalse(t);
        a.update(Config.reloadTime.get("firstTower"));
        t = a.shoot();
        Assert.assertTrue(t);
        Assert.assertEquals(Config.reloadTime.get("firstTower"), a.getReloadTime(), 0.0001);
    }

    @Test
    public void testDistance() {
        a.setPosition(0, 0, Align.center);
        float dist = a.distance(new Vector3(30, 40, 0));
        Assert.assertEquals(50, dist, 0.001);
        dist = Entity.distance(new Vector3(30, 40, 0), new Vector3(0, 0, 0));
        Assert.assertEquals(50, dist, 0.001);
        b.setX(40, Align.center);
        b.setY(30, Align.center);
        dist = a.distance(b);
        Assert.assertEquals(50, dist, 0.001);
    }
}

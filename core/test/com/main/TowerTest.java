package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TowerTest {
    Tower a;
    Tower b;

    @Before
    public void beforeEach() {
        a = new Tower("firstTower", 0, false);
    }

    @Test
    public void testTowerConstructor() {
        b = new Tower("T 12 mainTower 0 10.0 12.0 2.0 100.0", false);
        Assert.assertTrue(a.entityType == Entity.Type.TOWER);
        Assert.assertTrue(b.entityType == Entity.Type.TOWER);
        Assert.assertEquals(12, b.getId());
        Assert.assertEquals(0, b.getPlayerId());
        Assert.assertEquals(10, b.getX(Align.center), 0.0001);
        Assert.assertEquals(12, b.getY(Align.center), 0.0001);
        Assert.assertEquals(2, b.getReloadTime(), 0.001);
        Assert.assertEquals(100, b.getHP(), 0.0001);
        Assert.assertEquals("mainTower", b.getType());
    }

    @Test
    public void testToString() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        String x = a.toString();
        int id = a.getId();
        Assert.assertEquals("T "+id+" firstTower 0 10.0 12.0 1.0 120.0", x.trim());
    }

    @Test
    public void testGridUpdate() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        Vector3 x = a.gridUpdate();
        Assert.assertEquals(10, x.x, 0.0001);
        Assert.assertEquals(12, x.y, 0.0001);
        Assert.assertEquals(1, x.z, 0.0001);
    }
}

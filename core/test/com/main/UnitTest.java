package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class UnitTest {
    MapActor map;
    Unit a;
    Unit b;

    @Before
    public void beforeEach() {
        map = new MapActor(200, 100, "map0", false);
        a = new Unit("firstUnit", 0, map, false);
    }


    @Test
    public void testUnitConstructor() {
        b = new Unit("U 12 firstUnit 0 10.0 12.0 2.0 100.0 50.0 70.0", map, false);
        Assert.assertTrue(a.entityType == Entity.Type.UNIT);
        Assert.assertTrue(b.entityType == Entity.Type.UNIT);
        Assert.assertEquals(12, b.getId());
        Assert.assertEquals(0, b.getPlayerId());
        Assert.assertEquals(10, b.getX(Align.center), 0.0001);
        Assert.assertEquals(12, b.getY(Align.center), 0.0001);
        Assert.assertEquals(2, b.getReloadTime(), 0.001);
        Assert.assertEquals(100, b.getHP(), 0.0001);
        Assert.assertEquals("firstUnit", b.getType());
        for(int i = 0; i < 100; ++i) {
            b.act(0.1f);
        }
        Assert.assertEquals(50, b.getX(Align.center), 0.001);
        Assert.assertEquals(70, b.getY(Align.center), 0.001);
    }


    @Test
    public void testSetState() {
        a.setState("U 12 firstUnit 0 10.0 12.0 2.0 100.0 50.0 70.0");
        Assert.assertEquals(10, a.getX(Align.center), 0.0001);
        Assert.assertEquals(12, a.getY(Align.center), 0.0001);
        Assert.assertEquals(2, a.getReloadTime(), 0.001);
        Assert.assertEquals(100, a.getHP(), 0.0001);
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
        }
        Assert.assertEquals(50, a.getX(Align.center), 0.001);
        Assert.assertEquals(70, a.getY(Align.center), 0.001);
    }



    @Test
    public void testToString() {
        a.setX(10f, Align.center);
        a.setY(12f, Align.center);
        String x = a.toString();
        int id = a.getId();
        Assert.assertEquals("U "+id+" firstUnit 0 10.0 12.0 1.0 100.0 32.0 32.0", x.trim());
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
    public void testGoToPosition() {
        a.goToPosition(new Vector3(50, 70, 0));
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
        }
        Assert.assertEquals(32, a.getX(Align.center), 0.001);
        Assert.assertEquals(32, a.getY(Align.center), 0.001);
        a.allowTargetChanging(true);
        a.goToPosition(new Vector3(50, 70, 0));
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
        }
        Assert.assertEquals(50, a.getX(Align.center), 16);
        Assert.assertEquals(70, a.getY(Align.center), 16);
    }

    @Test
    public void testSetPosition() {
        a.setPosition(50, 70, Align.center);
        for(int i = 0; i < 100; ++i) {
            a.act(0.1f);
        }
        Assert.assertEquals(50, a.getX(Align.center), 0.001);
        Assert.assertEquals(70, a.getY(Align.center), 0.001);
    }
}

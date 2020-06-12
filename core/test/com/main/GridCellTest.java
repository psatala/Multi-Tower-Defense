package com.main;

import com.badlogic.gdx.math.Vector3;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class GridCellTest {
    GridCell a;
    static MapActor map;

    @BeforeClass
    static public void before() {
        map = new MapActor(1000, 500, "map0", false);
    }

    @Before
    public void beforeEach() {
        a = new GridCell(10, 20, 30, 40, map, false);
    }

    @Test
    public void testGetCenter() {
        Vector3 x = a.getCenter();
        Assert.assertEquals(25, x.x, 0.0001);
        Assert.assertEquals(40, x.y, 0.0001);
    }

    @Test
    public void testEmptyAndBlocked() {
        Assert.assertTrue(a.isEmpty());
        Assert.assertFalse(a.isBlocked());
        a.setBlocked(true);
        Assert.assertFalse(a.isEmpty());
        Assert.assertTrue(a.isBlocked());
        a.setBlocked(false);
        Assert.assertFalse(a.isEmpty());
        Assert.assertFalse(a.isBlocked());
        a.setBlocked(true);
        Assert.assertFalse(a.isEmpty());
        Assert.assertTrue(a.isBlocked());
        a.setEmpty(true);
        Assert.assertTrue(a.isEmpty());
        Assert.assertFalse(a.isBlocked());
    }
}

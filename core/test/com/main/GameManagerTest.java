package com.main;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Align;
import com.main.Networking.GameClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

import static org.junit.Assert.*;

public class GameManagerTest {
    GameManager a;

    @Before
    public void beforeEach() {
        a = new GameManager(0);
    }

    @Test
    public void addObserver() {
        GameClient gameClient = new GameClient(54545, 54545, 54546, 54546, 500);
        a.addObserver(gameClient);
        Assert.assertEquals(gameClient, a.observer);
    }

    @Test
    public void testDestroyingObjects() {
        Tower t1 = new Tower("mainTower", 0, false);
        t1.setId(0);
        Tower t2 = new Tower("firstTower", 0, false);
        t2.setId(1);
        MapActor map = new MapActor(1000, 500, "map0", false);
        a.map = map;
        Unit u = new Unit("firstUnit", 0, map, false);
        u.setId(2);
        a.towers.add(t1);
        a.towers.add(t2);
        Assert.assertEquals(2, a.towers.size());
        a.units.add(u);
        Vector<String> objects = new Vector<>();
        objects.add("U 2");
        objects.add("T 1");
        a.deleteKilledObjects(objects);
        Assert.assertEquals(1, a.towers.size());
        Assert.assertEquals(1, a.units.size());
        Assert.assertEquals(1, a.towers.get(0).id);
    }

    @Test
    public void testObjectExists() {
        Vector<String> objects = new Vector<>();
        objects.add("U 2");
        objects.add("T 1");
        Assert.assertTrue(a.objectExists(objects, 2, 'U'));
        Assert.assertFalse(a.objectExists(objects, 1, 'U'));
    }


    @Test
    public void testSelectUnits() {
        MapActor map = new MapActor(1000, 500, "map0", false);
        a.units.add(new Unit("firstUnit", 0, map, false));
        a.units.add(new Unit("firstUnit", 0, map, false));
        a.units.get(0).setPosition(50, 50, Align.center);
        a.units.get(1).setPosition(150, 150, Align.center);
        a.selectUnits(new Rectangle(0, 0, 60, 60));
        Assert.assertTrue(a.units.get(0).isTargetChangeable());
        Assert.assertFalse(a.units.get(1).isTargetChangeable());
    }



}
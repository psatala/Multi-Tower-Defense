package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SuperManagerTest {
    SuperManager a;

    @Before
    public void beforeEach() {
        a = new SuperManager();
    }

    @Test
    public void updateFight() {
        a.spawnTower(100, 100, "mainTower", 1);
        a.spawnUnit(200, 200, "firstUnit", 0);
        for(int i = 0; i < 100; ++i)
            a.updateFight();
        Assert.assertEquals(0, a.units.size());
    }

    @Test
    public void testFindTargetAndShoot() {
        a.spawnTower(100, 110, "mainTower", 1);
        a.spawnUnit(200, 220, "firstUnit", 0);
        a.towers.get(0).update(3);
        a.findTargetAndShoot(a.towers.get(0));
        Assert.assertEquals(1, a.missiles.size());
        Missile m = a.missiles.get(0);
        Assert.assertEquals(100, m.getX(Align.center), 0.001);
        Assert.assertEquals(110, m.getY(Align.center), 0.001);
        Assert.assertEquals(200, m.getTarget().x, 0.001);
        Assert.assertEquals(220, m.getTarget().y, 0.001);
    }


    @Test
    public void testSpawnUnit() {
        a.spawnUnit(100, 200, "firstUnit", 0);
        Assert.assertEquals(1, a.units.size());
        Unit u = a.units.get(0);
        Assert.assertEquals(100, u.getX(Align.center), 0.001);
        Assert.assertEquals(200, u.getY(Align.center), 0.001);
    }

    @Test
    public void testSpawnTower() {
        a.spawnTower(100, 200, "firstTower", 0);
        Assert.assertEquals(1, a.towers.size());
        Tower t = a.towers.get(0);
        Assert.assertEquals(100, t.getX(Align.center), 0.001);
        Assert.assertEquals(200, t.getY(Align.center), 0.001);
    }


    @Test
    public void testSendUnitTo() {
        a.spawnUnit(200, 220, "firstUnit", 0);
        a.spawnUnit(200, 220, "firstUnit", 0);
        a.spawnUnit(200, 220, "firstUnit", 0);
        a.units.get(0).setId(0);
        a.units.get(1).setId(1);
        a.units.get(2).setId(2);
        a.sendUnitTo(1, new Vector3(100, 110, 0));
        for(int i = 0; i < 100; ++i)
            a.updateFight();
        Assert.assertEquals(200, a.units.get(0).getX(Align.center), 0.001);
        Assert.assertEquals(220, a.units.get(0).getY(Align.center), 0.001);
        Assert.assertEquals(100, a.units.get(1).getX(Align.center), 17);
        Assert.assertEquals(110, a.units.get(1).getY(Align.center), 17);
        Assert.assertEquals(200, a.units.get(2).getX(Align.center), 0.001);
        Assert.assertEquals(220, a.units.get(2).getY(Align.center), 0.001);
    }
}

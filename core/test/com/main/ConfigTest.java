package com.main;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void testConfig() {
        Assert.assertEquals(1, (int)Config.objectCost.get("test_map1"));
        Assert.assertEquals(2, (int)Config.objectReward.get("test_map1"));
        Assert.assertEquals(3.1, (float)Config.width.get("test_map1"), 0.001);
        Assert.assertEquals(4.2, (float)Config.height.get("test_map1"), 0.001);
        Assert.assertEquals(5, (int)Config.damage.get("test_map1"));
        Assert.assertEquals(6.3, (float)Config.range.get("test_map1"), 0.001);
        Assert.assertEquals(7.4, (float)Config.reloadTime.get("test_map1"), 0.001);
        Assert.assertEquals(8, (int)Config.hp.get("test_map1"));
        Assert.assertEquals(9.5, (float)Config.speed.get("test_map1"), 0.001);
        Assert.assertEquals("xxx", Config.fullTexture.get("test_map1"));
        Assert.assertEquals("maps/map0.png", Config.representativeTexture.get("test_map1"));
        Assert.assertTrue(Config.mapGrid.get("test_map1")[1][1]);
        Assert.assertTrue(Config.mapGrid.get("test_map1")[4][1]);
        Assert.assertTrue(Config.mapGrid.get("test_map1")[2][3]);
        Assert.assertTrue(Config.mapGrid.get("test_map1")[3][7]);
        Assert.assertTrue(Config.mapGrid.get("test_map1")[1][9]);
        Assert.assertFalse(Config.mapGrid.get("test_map1")[0][0]);
        Assert.assertFalse(Config.mapGrid.get("test_map1")[4][9]);
        Assert.assertFalse(Config.mapGrid.get("test_map1")[2][2]);
        Assert.assertFalse(Config.mapGrid.get("test_map1")[2][6]);
        Assert.assertFalse(Config.mapGrid.get("test_map1")[0][9]);
    }
}

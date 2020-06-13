package com.main;

import com.badlogic.gdx.math.Vector3;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Vector;

import static java.lang.Math.abs;

public class MapActorTest {
    MapActor a;
    MapActor b;


    @Before
    public void beforeEach() {
        a = new MapActor(100, 50, "test_map1", false);
        b = new MapActor(100, 50, "test_map2", false);
    }


    @Test
    public void testGridCoords() {
        Vector3 x = a.getGridCoords(new Vector3(68, 55, 0));
        Vector3 y = b.getGridCoords(28, 37);
        Assert.assertEquals(6, x.x, 0.001);
        Assert.assertEquals(5, x.y, 0.001);
        Assert.assertEquals(2, y.x, 0.001);
        Assert.assertEquals(3, y.y, 0.001);

    }


    @Test
    public void testUpdateGrid() {
        Assert.assertTrue(a.isPositionBlocked(15, 25));
        Assert.assertFalse(a.isPositionEmpty(15, 25));
        Assert.assertFalse(a.isPositionBlocked(5, 5));
        Assert.assertTrue(a.isPositionEmpty(5, 5));
        Assert.assertFalse(a.isPositionBlocked(5, 15));
        Assert.assertTrue(a.isPositionEmpty(5, 15));
        Vector<Vector3> temp = new Vector<>();
        temp.add(new Vector3(5, 15, 0));
        temp.add(new Vector3(5, 5, 1));
        temp.add(new Vector3(15, 25, 0));
        a.updateGrid(temp);
        Assert.assertFalse(a.isPositionBlocked(5, 15));
        Assert.assertFalse(a.isPositionEmpty(5, 15));
        Assert.assertTrue(a.isPositionBlocked(5, 5));
        Assert.assertFalse(a.isPositionEmpty(5, 5));
        Assert.assertTrue(a.isPositionBlocked(15, 25));
        Assert.assertFalse(a.isPositionEmpty(15, 25));
    }


    @Test
    public void testBFS() {
        Vector<Vector3> test1Waypoints = a.BFS(new Vector3(1, 49, 0), new Vector3(99, 1, 0));
        Vector<Vector3> test2Waypoints = b.BFS(new Vector3(1, 49, 0), new Vector3(99, 1, 0));

        //Test 1 ground truth
        Vector<Vector3> correctWaypoints1 = new Vector<Vector3>();
        correctWaypoints1.add(new Vector3(5, 45, 0));
        correctWaypoints1.add(new Vector3(5, 35, 0));
        correctWaypoints1.add(new Vector3(5, 25, 0));
        correctWaypoints1.add(new Vector3(5, 15, 0));
        correctWaypoints1.add(new Vector3(5, 5, 0));
        correctWaypoints1.add(new Vector3(15, 5, 0));
        correctWaypoints1.add(new Vector3(25, 5, 0));
        correctWaypoints1.add(new Vector3(25, 15, 0));
        correctWaypoints1.add(new Vector3(25, 25, 0));
        correctWaypoints1.add(new Vector3(25, 35, 0));
        correctWaypoints1.add(new Vector3(25, 45, 0));
        correctWaypoints1.add(new Vector3(35, 45, 0));
        correctWaypoints1.add(new Vector3(45, 45, 0));
        correctWaypoints1.add(new Vector3(55, 45, 0));
        correctWaypoints1.add(new Vector3(65, 45, 0));
        correctWaypoints1.add(new Vector3(75, 45, 0));
        correctWaypoints1.add(new Vector3(85, 45, 0));
        correctWaypoints1.add(new Vector3(95, 45, 0));
        correctWaypoints1.add(new Vector3(95, 35, 0));
        correctWaypoints1.add(new Vector3(95, 25, 0));
        correctWaypoints1.add(new Vector3(85, 25, 0));
        correctWaypoints1.add(new Vector3(85, 15, 0));
        correctWaypoints1.add(new Vector3(85, 5, 0));
        correctWaypoints1.add(new Vector3(95, 5, 0));
        correctWaypoints1.add(new Vector3(99, 1, 0));

        //Test 2 ground truth
        Vector3 correctTarget2 = new Vector3(35, 5, 0);
        int numOfWaypoints = 9;

        //Asserts
        Assert.assertEquals("BFS returned wrong number of waypoints", correctWaypoints1.size(), test1Waypoints.size());
        for(int i = 0; i < correctWaypoints1.size(); ++i) {
            Assert.assertEquals("BFS returned wrong X coordinate of waypoint "+i, correctWaypoints1.elementAt(i).x, test1Waypoints.elementAt(i).x, 0.001);
            Assert.assertEquals("BFS returned wrong Y coordinate of waypoint "+i, correctWaypoints1.elementAt(i).y, test1Waypoints.elementAt(i).y, 0.001);
        }

        Assert.assertEquals("BFS returned wrong number of waypoints when no path was available", numOfWaypoints, test2Waypoints.size());
        for(int i = 1; i < test2Waypoints.size(); ++i) {
            Assert.assertTrue("Path returned by BFS is not continuous", abs(test2Waypoints.elementAt(i).x - test2Waypoints.elementAt(i-1).x) < 20);
            Assert.assertTrue("Path returned by BFS is not continuous", abs(test2Waypoints.elementAt(i).y - test2Waypoints.elementAt(i-1).y) < 20);
        }
        Vector3 test2Target = test2Waypoints.elementAt(test2Waypoints.size()-1);
        Assert.assertTrue("Movement target returned by BFS should be as close as possible to the original target", abs(correctTarget2.x - test2Target.x) <= 10);
        Assert.assertTrue("Movement target returned by BFS should be as close as possible to the original target", abs(correctTarget2.y - test2Target.y) <= 10);
    }


    @Test
    public void testLineBlocked() {
        Assert.assertTrue(a.isLineBlocked(new Vector3(5, 45, 0), new Vector3(95, 45, 0)));
        Assert.assertTrue(a.isLineBlocked(new Vector3(5, 35, 0), new Vector3(15, 5, 0)));
        Assert.assertFalse(a.isLineBlocked(new Vector3(5, 5, 0), new Vector3(5, 45, 0)));
        Assert.assertTrue(a.isLineBlocked(new Vector3(95, 36, 0), new Vector3(65, 5, 0)));
        Assert.assertFalse(a.isLineBlocked(new Vector3(95, 34, 0), new Vector3(65, 5, 0)));
    }


    @Test
    public void testSmoothPath() {
        Vector<Vector3> correctWaypoints1 = new Vector<Vector3>();
        correctWaypoints1.add(new Vector3(5, 45, 0));
        correctWaypoints1.add(new Vector3(5, 35, 0));
        correctWaypoints1.add(new Vector3(5, 25, 0));
        correctWaypoints1.add(new Vector3(5, 15, 0));
        correctWaypoints1.add(new Vector3(5, 5, 0));
        correctWaypoints1.add(new Vector3(15, 5, 0));
        correctWaypoints1.add(new Vector3(25, 5, 0));
        correctWaypoints1.add(new Vector3(25, 15, 0));
        correctWaypoints1.add(new Vector3(25, 25, 0));
        correctWaypoints1.add(new Vector3(25, 35, 0));
        correctWaypoints1.add(new Vector3(25, 45, 0));
        correctWaypoints1.add(new Vector3(35, 45, 0));
        correctWaypoints1.add(new Vector3(45, 45, 0));
        correctWaypoints1.add(new Vector3(55, 45, 0));
        correctWaypoints1.add(new Vector3(65, 45, 0));
        correctWaypoints1.add(new Vector3(75, 45, 0));
        correctWaypoints1.add(new Vector3(85, 45, 0));
        correctWaypoints1.add(new Vector3(95, 45, 0));
        correctWaypoints1.add(new Vector3(95, 35, 0));
        correctWaypoints1.add(new Vector3(95, 24, 0));
        correctWaypoints1.add(new Vector3(85, 25, 0));
        correctWaypoints1.add(new Vector3(85, 15, 0));
        correctWaypoints1.add(new Vector3(85, 5, 0));
        correctWaypoints1.add(new Vector3(95, 5, 0));
        correctWaypoints1.add(new Vector3(99, 1, 0));
        Vector<Vector3> x = a.smoothPath(correctWaypoints1);
        Assert.assertEquals(9, x.size());
        Assert.assertEquals(5, x.elementAt(0).x, 0.001);
        Assert.assertEquals(45, x.elementAt(0).y, 0.001);
        Assert.assertEquals(5, x.elementAt(1).x, 0.001);
        Assert.assertEquals(5, x.elementAt(1).y, 0.001);
        Assert.assertEquals(25, x.elementAt(2).x, 0.001);
        Assert.assertEquals(5, x.elementAt(2).y, 0.001);
        Assert.assertEquals(25, x.elementAt(3).x, 0.001);
        Assert.assertEquals(45, x.elementAt(3).y, 0.001);
        Assert.assertEquals(95, x.elementAt(4).x, 0.001);
        Assert.assertEquals(45, x.elementAt(4).y, 0.001);
        Assert.assertEquals(95, x.elementAt(5).x, 0.001);
        Assert.assertEquals(24, x.elementAt(5).y, 0.001);
        Assert.assertEquals(85, x.elementAt(6).x, 0.001);
        Assert.assertEquals(25, x.elementAt(6).y, 0.001);
        Assert.assertEquals(85, x.elementAt(7).x, 0.001);
        Assert.assertEquals(5, x.elementAt(7).y, 0.001);
        Assert.assertEquals(99, x.elementAt(8).x, 0.001);
        Assert.assertEquals(1, x.elementAt(8).y, 0.001);
    }


    @Test
    public void testFindPath() {
        Vector<Vector3> x = a.findPath(new Vector3(9, 42, 0), new Vector3(47, 13, 0));
        Assert.assertEquals(7, x.size());
        Assert.assertEquals(9, x.elementAt(0).x, 0.001);
        Assert.assertEquals(42, x.elementAt(0).y, 0.001);
        Assert.assertEquals(5, x.elementAt(1).x, 0.001);
        Assert.assertEquals(5, x.elementAt(1).y, 0.001);
        Assert.assertEquals(25, x.elementAt(2).x, 0.001);
        Assert.assertEquals(5, x.elementAt(2).y, 0.001);
        Assert.assertEquals(25, x.elementAt(3).x, 0.001);
        Assert.assertEquals(45, x.elementAt(3).y, 0.001);
        Assert.assertEquals(95, x.elementAt(4).x, 0.001);
        Assert.assertEquals(45, x.elementAt(4).y, 0.001);
        Assert.assertEquals(95, x.elementAt(5).x, 0.001);
        Assert.assertEquals(25, x.elementAt(5).y, 0.001);
        Assert.assertEquals(47, x.elementAt(6).x, 0.001);
        Assert.assertEquals(13, x.elementAt(6).y, 0.001);
        Vector<Vector3> y = b.findPath(new Vector3(4, 2, 0), new Vector3(80, 45, 0));
        Assert.assertEquals(2, y.size());
        Assert.assertEquals(4, y.elementAt(0).x, 0.001);
        Assert.assertEquals(2, y.elementAt(0).y, 0.001);
        Assert.assertEquals(35, y.elementAt(1).x, 17);
        Assert.assertEquals(45, y.elementAt(1).y, 17);
    }
}

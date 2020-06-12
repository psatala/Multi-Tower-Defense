package com.main.Networking;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RoomListTest {

    RoomList roomList;
    GameRoom gameRoom;

    @Before
    public void setUp() throws Exception {
        roomList = new RoomList();
        gameRoom = new GameRoom("host", 4, GameRoom.LOCAL, -1, "host_player");
        roomList.add(gameRoom);
    }

    @Test
    public void add() {
        GameRoom gameRoom2 = new GameRoom("host2", 3, GameRoom.GLOBAL, 1, "host_player2");
        roomList.add(gameRoom2);

        Assert.assertEquals(gameRoom2, roomList.get(gameRoom2.roomID));
    }

    @Test
    public void putALL() {
        RoomList roomList2 = new RoomList();
        GameRoom gameRoom2 = new GameRoom("host2", 3, GameRoom.GLOBAL, 1, "host_player2");
        roomList2.add(gameRoom2);

        roomList.putALL(roomList2);

        Assert.assertEquals(gameRoom2, roomList.get(gameRoom2.roomID));
    }

    @Test
    public void get() {
        Assert.assertEquals(gameRoom, roomList.get(gameRoom.roomID));
    }

    @Test
    public void remove() {
        roomList.remove(gameRoom.roomID);

        Assert.assertNull(roomList.get(gameRoom.roomID));
    }

    @Test
    public void containsKey() {
        Assert.assertTrue(roomList.containsKey(gameRoom.roomID));
    }

    @Test
    public void getMaxKey() {
        Assert.assertTrue(roomList.containsKey(gameRoom.roomID));
    }
}
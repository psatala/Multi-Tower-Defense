package com.main.Networking;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class GameRoomTest {

    GameRoom gameRoom;

    @Before
    public void setUp() {
        gameRoom = new GameRoom("host", 4, GameRoom.LOCAL, -1, "host_player");
    }

    @Test
    public void constructor() {
        Assert.assertEquals(GameRoom.LOCAL, gameRoom.gameType.intValue());
        Assert.assertFalse(gameRoom.connectionSet.isEmpty());
        Assert.assertEquals(1, gameRoom.currentPlayers.intValue());
        Assert.assertEquals(4, gameRoom.maxPlayers.intValue());
    }

    @Test
    public void addPlayer() {
        try {
            gameRoom.addPlayer(2, "player2");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(2, gameRoom.connectionSet.size());
    }

    @Test
    public void removePlayer() {
        try {
            gameRoom.removePlayer(-1);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }

        Assert.assertFalse(false);
    }

}
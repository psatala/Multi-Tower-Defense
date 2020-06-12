package com.main.Networking.requests;

import com.main.Networking.GameRoom;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class CreateRoomRequestTest {

    @Test
    public void defaultConstructor() {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();

        Assert.assertEquals("", createRoomRequest.hostName);
        Assert.assertEquals(1, createRoomRequest.maxPlayers);
        Assert.assertEquals(GameRoom.GLOBAL, createRoomRequest.gameType);
    }

    @Test
    public void parametrisedConstructor() {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest("host", 4, GameRoom.LOCAL);

        Assert.assertEquals("host", createRoomRequest.hostName);
        Assert.assertEquals(4, createRoomRequest.maxPlayers);
        Assert.assertEquals(GameRoom.LOCAL, createRoomRequest.gameType);
    }
}
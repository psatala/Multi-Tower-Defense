package com.main.Networking;

import com.main.GameManager;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

import static org.junit.Assert.*;

public class LocalServerTest {

    GameClient gameClient;

    @Before
    public void setUp() throws Exception {
        gameClient = new GameClient(54545, 54545, 54546, 54546, 100);
        gameClient.playerName = "player1";
        gameClient.setActiveClient(GameRoom.LOCAL);
        GameManager gameManager = new GameManager(0);
        gameManager.addObserver(gameClient);
        new LocalServer(54546, 54546, "localhost", 2, gameManager);
    }

    @Test
    public void connectWithClient() {
        try {
            gameClient.activeClient.connect(100, InetAddress.getLoopbackAddress(), 54546, 54546);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Assert.assertTrue(gameClient.activeClient.isConnected());

    }
}
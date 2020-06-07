package com.main.Networking;

import com.main.GameManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

public class GameClientTest {

    GameClient gameClient;

    @Before
    public void setUp() {
        gameClient = new GameClient(54545, 54545, 54546, 54546, 100);
        gameClient.playerName = "player1";
    }

    @Test
    public void connectWithLocalServer() {

        try {
            gameClient.setActiveClient(GameRoom.LOCAL);
            GameManager gameManager = new GameManager(0);
            gameManager.addObserver(gameClient);
            new LocalServer(54546, 54546, "localhost", 2, gameManager);

            gameClient.activeClient.connect(100, InetAddress.getLoopbackAddress(), 54546, 54546);


            Assert.assertTrue(gameClient.activeClient.isConnected());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void connectWithMainServer() {
        try {
            gameClient.setActiveClient(GameRoom.GLOBAL);
            GameManager gameManager = new GameManager(0);
            gameManager.addObserver(gameClient);
            new MainServer(54545, 54545);

            gameClient.activeClient.connect(100, InetAddress.getLoopbackAddress(), 54545, 54545);

            Assert.assertTrue(gameClient.activeClient.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
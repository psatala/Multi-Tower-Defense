package com.main.Networking;

import com.main.GameManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.InetAddress;

public class MainServerTest {

    MainServer mainServer;

    @Before
    public void setUp() throws Exception {
        mainServer = new MainServer(54545, 54545);
    }

    @Test
    public void connectWithClient() {
        try {
            GameClient gameClient;
            gameClient = new GameClient(54545, 54545, 54546, 54546, 100);
            gameClient.playerName = "player1";

            gameClient.setActiveClient(GameRoom.GLOBAL);
            GameManager gameManager = new GameManager(0);
            gameManager.addObserver(gameClient);

            gameClient.activeClient.connect(100, InetAddress.getLoopbackAddress(), 54545, 54545);

            Assert.assertTrue(gameClient.activeClient.isConnected());

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @After
    public void tearDown() {
        mainServer.close();
        mainServer.stop();
    }
}
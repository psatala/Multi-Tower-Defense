package com.main;

import com.main.Networking.GameClient;
import org.junit.Assert;
import org.junit.Test;

public class GameManagerTest {

    @Test
    public void addObserver() {
        //assign
        GameManager gameManager = new GameManager(0);
        GameClient gameClient = new GameClient(54545, 54545, 54546, 54546, 500);

        //act
        gameManager.addObserver(gameClient);

        //assert
        Assert.assertEquals(gameClient, gameManager.observer);
    }

}
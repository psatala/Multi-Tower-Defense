package com.main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.main.Networking.GameClient;

import java.io.IOException;

public class PlayerLauncher {
    public static void main (String[] arg) {
        try {
            GameClient gameClient = new GameClient(54545, 54545, 54546, 54546, 500);
            final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
            cfg.title = "Multi Tower Defense";
            cfg.height = 720;
            cfg.width = 1080;
            cfg.resizable = false;
            gameClient.gameManager.addObserver(gameClient);
            new LwjglApplication(gameClient.gameManager, cfg);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}

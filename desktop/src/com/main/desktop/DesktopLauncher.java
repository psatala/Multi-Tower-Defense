package com.main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.main.GameManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Multi Tower Defense";
		cfg.height = 720;
		cfg.width = 1080;
		cfg.resizable = false;
		new LwjglApplication(new GameManager(0), cfg);
	}
}

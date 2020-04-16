package com.main.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.main.MainGameView;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		Config myConfig = new Config();
		cfg.title = myConfig.windowTitle;
		cfg.height = myConfig.windowHeight;
		cfg.width = myConfig.windowWidth;
		new LwjglApplication(new MainGameView(), cfg);
	}
}

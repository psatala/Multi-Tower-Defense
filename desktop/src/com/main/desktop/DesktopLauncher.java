package com.main.desktop;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.main.GameManager;
import com.main.SuperManager;

public class DesktopLauncher {
	public static void main (String[] arg) {
		SuperManager superMan = new SuperManager();
		final LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "Multi Tower Defense";
		cfg.height = 720;
		cfg.width = 1080;
		cfg.resizable = false;
		new LwjglApplication(superMan, cfg);
	}
}

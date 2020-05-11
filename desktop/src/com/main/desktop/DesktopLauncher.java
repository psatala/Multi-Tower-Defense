package com.main.desktop;

import com.main.Networking.GameServer;

import java.io.IOException;

public class DesktopLauncher {
	public static void main (String[] arg) {
		try {
			new GameServer(54545, 54545);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

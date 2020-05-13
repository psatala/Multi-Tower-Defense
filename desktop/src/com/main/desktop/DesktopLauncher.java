package com.main.desktop;

import com.main.Networking.MainServer;

import java.io.IOException;

public class DesktopLauncher {
	public static void main (String[] arg) {
		try {
			new MainServer(54545, 54545);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

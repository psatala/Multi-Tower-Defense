package com.main.desktop;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;


public class MyPacker {
    public static void pack () {
        TexturePacker.process("/home/piotr/Documents/staszic", "/home/piotr/Multi-Tower-Defense/core/assets/", "staszic");
    }
}
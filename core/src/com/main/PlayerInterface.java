package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlayerInterface extends Actor {
    private BitmapFont bigFont;

    private Pixmap pixmap;
    private Texture line;
    private Sprite lineSprite;

    public PlayerInterface() {
        bigFont = new BitmapFont();
        bigFont.setColor(Color.WHITE);
        pixmap = new Pixmap(1080, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        line = new Texture(pixmap);
        pixmap.dispose();
        lineSprite = new Sprite(line);
    }


    @Override
    public void draw(Batch batch, float alpha) {
        bigFont.draw(batch, "Player 1", 530, 710);
        bigFont.draw(batch, "Coins: You're broke.", 940, 710);
        lineSprite.setPosition(0, 686);
        lineSprite.draw(batch);
    }

    public void dispose() {
        bigFont.dispose();
        line.dispose();
    }
}

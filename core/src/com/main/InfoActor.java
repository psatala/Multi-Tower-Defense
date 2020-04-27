package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class InfoActor extends Actor {
    private BitmapFont bigFont;
    private Pixmap pixmap;
    private Texture line;
    private Sprite lineSprite;
    private int coins = 1000;
    private int playerId;

    public InfoActor(float w, float h, float y, int playerId) {
        this.setBounds(0, y, w, h);
        this.playerId = playerId;
        bigFont = new BitmapFont();
        bigFont.setColor(Color.WHITE);
        pixmap = new Pixmap((int)w, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        line = new Texture(pixmap);
        pixmap.dispose();
        lineSprite = new Sprite(line);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        bigFont.draw(batch, "Player "+playerId, 530, 710);
        bigFont.draw(batch, "Coins: "+String.valueOf(coins), 940, 710);
        lineSprite.setPosition(0, 686);
        lineSprite.draw(batch);
    }

    public void dispose() {
        bigFont.dispose();
        line.dispose();
    }

    public boolean spendCoins(int n) {
        if(coins < n)
            return false;
        coins -= n;
        return true;
    }

    public void addCoins(int n) {
        coins += n;
    }
}
package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;


public class PlayerInterface extends Group {
    final private float topBarHeight = 34;
    private InfoActor info;
    public MapActor map;
    private Skin skin;

    public PlayerInterface(MainGameView gameView) {
        this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        info = new InfoActor(this.getWidth(), topBarHeight, this.getHeight() - topBarHeight);
        map = new MapActor(this.getWidth(), this.getHeight() - topBarHeight, gameView);
        this.addActor(info);
        this.addActor(map);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        addButton(20, info.getY()+4, 100, topBarHeight-8, "Select Units", MapActor.Mode.SELECT);
        addButton(140, info.getY()+4, 100, topBarHeight-8, "Move Units", MapActor.Mode.MOVE);
        addButton(260, info.getY()+4, 100, topBarHeight-8, "Build tower", MapActor.Mode.BUILD);
    }


    private void addButton(float x, float y, float w, float h, String text, final MapActor.Mode mode) {
        final TextButton button = new TextButton(text, skin, "default");
        button.setBounds(x, y, w, h);
        this.addActor(button);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                map.setMode(mode);
            }
        });
    }
}


class InfoActor extends Actor {
    private BitmapFont bigFont;
    private Pixmap pixmap;
    private Texture line;
    private Sprite lineSprite;
    private int coins = 1000;

    public InfoActor(float w, float h, float y) {
        this.setBounds(0, y, w, h);
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
        bigFont.draw(batch, "Player 1", 530, 710);
        bigFont.draw(batch, "Coins: "+String.valueOf(coins), 940, 710);
        lineSprite.setPosition(0, 686);
        lineSprite.draw(batch);
    }

    public void dispose() {
        bigFont.dispose();
        line.dispose();
    }
}
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

public class InfoActor extends Actor {
    final public static float topBarHeight = 34;
    private GameManager gameManager;
    private Group infoGroup;
    private BitmapFont bigFont;
    private Pixmap pixmap;
    private Texture line;
    private Sprite lineSprite;
    private Skin skin;
    private int coins;
    private int playerId;

    public InfoActor(float w, float h, GameManager gameManager, int playerId) {
        coins = Config.startingCoins;
        setBounds(0, h-topBarHeight, w, topBarHeight);
        this.gameManager = gameManager;
        this.playerId = playerId;
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        bigFont = new BitmapFont();
        bigFont.setColor(Color.WHITE);
        pixmap = new Pixmap((int)w, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        line = new Texture(pixmap);
        pixmap.dispose();
        lineSprite = new Sprite(line);
        infoGroup = new Group();
        infoGroup.addActor(this);
        addButton(20, getY()+4, 100, topBarHeight-8, "Select Units", MapActor.Mode.SELECT);
        addButton(140, getY()+4, 100, topBarHeight-8, "Move Units", MapActor.Mode.MOVE);
        addButton(260, getY()+4, 100, topBarHeight-8, "Build tower", MapActor.Mode.BUILD);
    }

    @Override
    public void draw(Batch batch, float alpha) {
        bigFont.draw(batch, "Player "+playerId, 530, 710);
        bigFont.draw(batch, "Coins: "+coins, 940, 710);
        lineSprite.setPosition(0, 686);
        lineSprite.draw(batch);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
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

    private void addButton(float x, float y, float w, float h, String text, final MapActor.Mode mode) {
        final TextButton button = new TextButton(text, skin, "default");
        button.setBounds(x, y, w, h);
        infoGroup.addActor(button);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                gameManager.setMode(mode);
            }
        });
    }

    public Group getInfoGroup() {
        return infoGroup;
    }
}
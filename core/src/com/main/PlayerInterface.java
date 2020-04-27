package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.Vector;


public class PlayerInterface extends Group {
    final private float topBarHeight = 34;
    private InfoActor info;
    public MapActor map;
    private Skin skin;
    private int playerId;

    public PlayerInterface(MainGameView gameView, int color) {
        playerId = color;
        this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        info = new InfoActor(this.getWidth(), topBarHeight, this.getHeight() - topBarHeight);
        map = new MapActor(this.getWidth(), this.getHeight() - topBarHeight, gameView, color);
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

    public void updateGrid(Vector<Vector3> updates) {
        map.updateGrid(updates);
    }

    public boolean spendCoins(int n) {
        return info.spendCoins(n);
    }

    public void addCoins(int n) {
        info.addCoins(n);
    }
}
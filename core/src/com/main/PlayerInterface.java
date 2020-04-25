package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class PlayerInterface extends Group {
    final private float topBarHeight = 34;
    private InfoActor info;
    private MapActor map;
    private Skin skin;

    public PlayerInterface(MainGameView gameView) {
        this.setBounds(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        info = new InfoActor(this.getWidth(), topBarHeight, this.getHeight() - topBarHeight);
        map = new MapActor(this.getWidth(), this.getHeight() - topBarHeight, gameView);
        this.addActor(info);
        this.addActor(map);
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        addButton(20, info.getY()+4, 100, topBarHeight-8, "Select Units", 1);
        addButton(140, info.getY()+4, 100, topBarHeight-8, "Move Units", 0);
    }


    private void addButton(float x, float y, float w, float h, String text, final int mode) {
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


class MapActor extends Actor {
    private Vector3 selection;
    private Rectangle selectRect;
    private boolean drawSelection = false;
    private MainGameView gameManager;
    private ShapeRenderer renderer;
    private int mode;

    public MapActor(float w, float h, MainGameView gameView) {
        gameManager = gameView;
        renderer = new ShapeRenderer();
        this.setBounds(0, 0, w, h);
        this.addListener(createInterfaceListener());
    }

    @Override
    public void draw(Batch batch, float alpha) {

        if (drawSelection) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
            renderer.rect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
            renderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
    }

    public void setMode(int m) {
        mode = m;
    }

    private InputListener createInterfaceListener() {
        return new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                selection = new Vector3(x, y, 0);
                if(mode == 1){
                    selectRect = new Rectangle(selection.x, selection.y, 0, 0);
                    drawSelection = true;
                }
                else if(mode == 0){
                    gameManager.sendUnitsTo(selection);
                    drawSelection = false;
                }
                return true;
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(!drawSelection)
                    return;
                Vector3 dragSelect = new Vector3(x, y, 0);
                selectRect.x = min(selection.x, dragSelect.x);
                selectRect.y = min(selection.y, dragSelect.y);
                selectRect.width = abs(selection.x - dragSelect.x);
                selectRect.height = abs(selection.y - dragSelect.y);
                return;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(!drawSelection)
                    return;
                drawSelection = false;
                gameManager.selectUnits(selectRect);
            }
        };
    }
}
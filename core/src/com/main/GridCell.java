package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;


public class GridCell extends Actor {
    private boolean isEmpty = true;
    private boolean isBlocked = false;
    private boolean isHighlighted = false;
    private MapActor map;
    private ShapeRenderer renderer;

    public GridCell(float x, float y, float w, float h, MapActor m) {
        map = m;
        setBounds(x, y, w, h);
        renderer = new ShapeRenderer();
        addListener(createHoverListener());
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(map.mode == MapActor.Mode.BUILD && (!isEmpty || isHighlighted)) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            if(!isEmpty){
                renderer.setColor(new Color(1.0f, 0.0f, 0.0f, 0.3f));
                renderer.rect(getX(), getY(), getWidth(), getHeight());
            }
            if(isHighlighted){
                renderer.setColor(new Color(0.0f, 1.0f, 0.0f, 0.3f));
                renderer.rect(getX(), getY(), getWidth(), getHeight());
            }
            renderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
    }

    private ClickListener createHoverListener() {
        return new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isHighlighted = true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isHighlighted = false;
            }
        };
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean x) {
        isBlocked = x;
        if(isBlocked)
            isEmpty = false;
    }

    public void setEmpty(boolean x) {
        isEmpty = x;
        if(!isEmpty)
            isBlocked = false;
    }

    public Vector3 getCenter() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }
}

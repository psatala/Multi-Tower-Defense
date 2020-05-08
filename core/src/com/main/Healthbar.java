package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Healthbar extends Actor {
    private final float height = 5;
    private Pixmap greenPartPixmap;
    private Texture greenPart;
    private Pixmap redPartPixmap;
    private Texture redPart;
    private float hp;
    private float maxHP;
    private boolean isDrawable;

    public Healthbar(float maximumHP, float w, boolean drawable) {
        setBounds(0, 0, w, height);
        isDrawable = drawable;
        maxHP = maximumHP;
        hp = maxHP;
        if(isDrawable)
            createTexture();
    }

    private void createTexture() {
        greenPartPixmap = new Pixmap((int)height, (int)height, Pixmap.Format.RGBA8888);
        redPartPixmap = new Pixmap((int)height, (int)height, Pixmap.Format.RGBA8888);
        greenPartPixmap.setColor(Color.GREEN);
        greenPartPixmap.fillRectangle(0, 0, (int)height, (int)height);
        greenPart = new Texture(greenPartPixmap);
        redPartPixmap.setColor(Color.RED);
        redPartPixmap.fillRectangle(0, 0, (int)height, (int)height);
        redPart = new Texture(redPartPixmap);
        greenPartPixmap.dispose();
        redPartPixmap.dispose();
    }

    //Returns true if object is dead
    public boolean damage(float healthPoints) {
        hp -= healthPoints;
        if(hp <= 0) {
            hp = 0;
            return true;
        }
        return false;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(isDrawable) {
            batch.draw(greenPart, getX(), getY(), getWidth()*(hp/maxHP), getHeight());
            batch.draw(redPart, getX()+getWidth()*(hp/maxHP), getY(), getWidth()*(1-hp/maxHP), getHeight());
        }
    }

    public float getHP() {
        return hp;
    }

    public void setHP(float hp) {
        this.hp = hp;
    }
}

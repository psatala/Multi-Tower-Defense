package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class Healthbar{
    private Vector3 position;
    private float width;
    private final float height = 5;
    private float hp;
    private float maxHP;

    public Healthbar(float maximumHP, float x, float y, float w) {
        position = new Vector3(x, y, 0);
        width = w;
        maxHP = maximumHP;
        hp = maxHP;
    }

    public void setWidth(float w) {
        width = w;
    }

    public void setPosition(Vector3 pos) {
        position.set(pos);
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

    public void draw(ShapeRenderer renderer) {
        renderer.setColor(Color.GREEN);
        renderer.rect(position.x-width/2, position.y, width*(hp/maxHP), height);
        renderer.setColor(Color.RED);
        renderer.rect(position.x-width/2+width*(hp/maxHP), position.y, width*(1-hp/maxHP), height);
    }

    public float getHP() {
        return hp;
    }
}

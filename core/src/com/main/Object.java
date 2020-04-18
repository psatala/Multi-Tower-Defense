package com.main;

import com.badlogic.gdx.math.Vector3;

public class Object {
    static int idCounter = 0;

    protected int id;
    protected Vector3 position;
    protected float width;
    protected float height;
    protected int color;
    protected float hp;
    protected float damage;



    public Object(float x, float y) {
        position = new Vector3(x, y, 0);
        id = idCounter;
        idCounter++;
    }

    public Vector3 getPosition() {
        return position;
    }

    public float getX() {
        return position.x;
    }

    public float getY() {
        return position.y;
    }
}

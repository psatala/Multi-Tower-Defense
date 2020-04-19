package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Object{
    static int idCounter = 0;

    final private float reloadTime = 1;
    final public float range = 300;

    protected int id;
    protected Vector3 position;
    protected float width;
    protected float height;
    protected int color;
    protected float damage = 5;
    protected float reloadTimeLeft;
    protected Healthbar healthbar;



    public Object(float x, float y, int color) {
        position = new Vector3(x, y, 0);
        this.color = color;
        id = idCounter;
        idCounter++;
        height = width = 0;
        reloadTimeLeft = reloadTime;

        healthbar = new Healthbar(100, x, y, width);
    }

    public void draw(ShapeRenderer renderer) {
        healthbar.draw(renderer);
    }

    public boolean damage(float healthPoints) {
        return healthbar.damage(healthPoints);
    }

    public void update() {
        reloadTimeLeft -= Gdx.graphics.getDeltaTime();
        reloadTimeLeft = max(0, reloadTimeLeft);
    }

    public boolean shoot() {
        if(reloadTimeLeft == 0) {
            reloadTimeLeft = reloadTime;
            return true;
        }
        return false;
    }

    public boolean isAlive() {
        return healthbar.getHP() > 0;
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

    public float distance(Vector3 pos) {
        return (float)sqrt(pow(position.x-pos.x, 2) + pow(position.y-pos.y, 2));
    }
}

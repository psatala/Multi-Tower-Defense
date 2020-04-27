package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Object extends Actor {
    static int idCounter = 0;

    final private float reloadTime = 1;
    final public float range = 300;

    protected TextureRegion textureRegion;

    protected int id;
    protected int color;
    protected float damage = 5;
    protected float reloadTimeLeft;
    protected Healthbar healthbar;
    protected String type;
    protected int cost = 0;
    protected int reward = 0;


    public Object(String type, int color) {
        textureRegion = new TextureRegion(new Texture(type+String.valueOf(color)+"0.png"));
        this.setBounds(0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        this.color = color;
        id = idCounter;
        idCounter++;
        reloadTimeLeft = reloadTime;

        healthbar = new Healthbar(100, this.getWidth());
        healthbar.setPosition(0, this.getHeight());
    }

    public Vector3 gridUpdate() {
        return new Vector3(0, 0, 0);
    }

    public int getCost() {
        return cost;
    }

    public int getReward() {
        return reward;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.draw(textureRegion, this.getX(), this.getY());
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


    public float distance(Vector3 pos) {
        return (float)sqrt(pow(this.getX(Align.center)-pos.x, 2) + pow(this.getY(Align.center)-pos.y, 2));
    }

    public float distance(Object object) {
        return (float)sqrt(pow(this.getX(Align.center)-object.getX(Align.center), 2) + pow(this.getY(Align.center)-object.getY(Align.center), 2));
    }

    @Override
    public void setPosition(float x, float y, int align) {
        super.setPosition(x, y, align);
        healthbar.setPosition(x, y+this.getHeight()/2, align);
    }

    @Override
    public boolean remove() {
        healthbar.remove();
        return super.remove();
    }
}

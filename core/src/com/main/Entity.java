package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;

import static java.lang.Math.max;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Entity extends Actor {
    static int idCounter = 0;

    private float reloadTime;
    private float range;
    private float damage;
    private int cost;
    private int reward;

    protected TextureRegion textureRegion;
    private Group objectGroup;

    protected int id;
    protected int playerId;
    protected float reloadTimeLeft;
    protected Healthbar healthbar;
    protected String type;


    public Entity(String type, int playerId) {
        textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.representativeTexture.get(type))));
        setBounds(0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        this.playerId = playerId;
        id = idCounter;
        idCounter++;
        this.type = type;
        reloadTime = Config.reloadTime.get(type);
        reloadTimeLeft = reloadTime;
        range = Config.range.get(type);
        damage = Config.damage.get(type);
        cost = Config.objectCost.get(type);
        reward = Config.objectReward.get(type);

        healthbar = new Healthbar(Config.hp.get(type), getWidth());
        healthbar.setPosition(0, getHeight());
        objectGroup = new Group();
        objectGroup.addActor(this);
        objectGroup.addActor(healthbar);
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
        batch.draw(textureRegion, getX(), getY());
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
        return (float)sqrt(pow(getX(Align.center)-pos.x, 2) + pow(getY(Align.center)-pos.y, 2));
    }

    public float distance(Vector3 a, Vector3 b) {
        return (float)sqrt(pow(a.x-b.x, 2) + pow(a.y-b.y, 2));
    }

    public float distance(Entity entity) {
        return (float)sqrt(pow(getX(Align.center)- entity.getX(Align.center), 2) + pow(getY(Align.center)- entity.getY(Align.center), 2));
    }

    @Override
    public void setPosition(float x, float y, int align) {
        healthbar.setPosition(x, y+getHeight()/2, align);
        super.setPosition(x, y, align);
    }

    @Override
    public boolean remove() {
        return objectGroup.remove();
    }

    public Group getObjectGroup() {
        return objectGroup;
    }

    public float getDamage() {
        return damage;
    }

    public float getRange() {
        return range;
    }
}

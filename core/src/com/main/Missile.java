package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.utils.Align;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;

public class Missile extends Actor {
    static int idCounter = 0;
    private float velocity;
    private Vector3 target;
    private float damage;
    private Texture texture;
    private boolean isFlying = true;
    private int id;
    private int playerId;
    private String type;

    public Missile(Entity target, Entity source, String type) {
        this.type = type;
        id = idCounter;
        idCounter++;
        texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
        velocity = Config.speed.get(type);
        this.target = new Vector3(target.getX(Align.center), target.getY(Align.center), 0);
        damage = source.getDamage();
        setBounds(0, 0, texture.getWidth(), texture.getHeight());
        setPosition(source.getX(Align.center), source.getY(Align.center), Align.center);

        MoveToAction moveAction = new MoveToAction();
        moveAction.setPosition(target.getX(Align.center), target.getY(Align.center));
        float travelTime = source.distance(target) / velocity;
        moveAction.setDuration(travelTime);
        RunnableAction completionAction = new RunnableAction(){
            public void run(){
                isFlying = false;
            }
        };
        addAction(sequence(moveAction, completionAction));
        playerId = source.playerId;
    }

    public Missile(String stateString) {
        String[] data = stateString.split(" ");
        id = Integer.parseInt(data[1]);
        type = data[2];
        playerId = Integer.parseInt(data[3]);
        target = new Vector3(Float.parseFloat(data[6]), Float.parseFloat(data[7]), 0);
        damage = Float.parseFloat(data[8]);
        texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
        velocity = Config.speed.get(type);
        setBounds(0, 0, texture.getWidth(), texture.getHeight());
        setX(Float.parseFloat(data[4]), Align.center);
        setY(Float.parseFloat(data[5]), Align.center);
        MoveToAction moveAction = new MoveToAction();
        moveAction.setPosition(target.x, target.y);
        float travelTime = Entity.distance(getPosition(Align.center), target) / velocity;
        moveAction.setDuration(travelTime);
        RunnableAction completionAction = new RunnableAction(){
            public void run(){
                isFlying = false;
            }
        };
        addAction(sequence(moveAction, completionAction));
    }

    public void setState(String stateString) {
        String[] data = stateString.split(" ");
        setX(Float.parseFloat(data[4]), Align.center);
        setY(Float.parseFloat(data[5]), Align.center);
    }

    public String toString() {
        String s = "M ";
        s += id + " ";
        s += type + " ";
        s += playerId + " ";
        s += getX(Align.center) + " ";
        s += getY(Align.center) + " ";
        s += target.x + " ";
        s += target.y + " ";
        s += damage + " ";
        s += "\n";
        return s;
    }

    public Vector3 getTarget() {
        return target;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.draw(texture, getX(), getY());
    }

    public boolean isAlive() {
        return isFlying;
    }

    public boolean hitObject(Entity entity) {
        float myX = getX(Align.center);
        float myY = getY(Align.center);
        if(myX < entity.getX() || myX >= entity.getX(Align.right))
            return false;
        if(myY < entity.getY() || myY >= entity.getY(Align.top))
            return false;
        return true;
    }

    public void targetHit() {
        isFlying = false;
    }

    public float getDamage(){
        return damage;
    }

    public int getPlayerId() {
        return playerId;
    }

    public int getId() {
        return id;
    }

    public Vector3 getPosition(int align) {
        Vector3 pos = new Vector3();
        pos.x = getX(align);
        pos.y = getY(align);
        return pos;
    }

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }
}

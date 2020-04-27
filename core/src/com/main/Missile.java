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
    static final float velocity = 300;
    private Vector3 target;
    private float damage;
    private Texture texture;
    private boolean isFlying = true;
    private int playerId;

    public Missile(Object target, Object source) {
        texture = new Texture(Gdx.files.internal("missile.png"));
        this.target = new Vector3(target.getX(Align.center), target.getY(Align.center), 0);
        damage = source.damage;
        this.setBounds(0, 0, texture.getWidth(), texture.getHeight());
        this.setPosition(source.getX(Align.center), source.getY(Align.center), Align.center);

        MoveToAction moveAction = new MoveToAction();
        moveAction.setPosition(target.getX(Align.center), target.getY(Align.center));
        float travelTime = source.distance(target) / velocity;
        moveAction.setDuration(travelTime);
        RunnableAction completionAction = new RunnableAction(){
            public void run(){
                isFlying = false;
            }
        };
        this.addAction(sequence(moveAction, completionAction));
        playerId = source.playerId;
    }

    public Vector3 getTarget() {
        return target;
    }

    @Override
    public void draw(Batch batch, float alpha) {
        batch.draw(texture, this.getX(), this.getY());
    }

    public boolean isAlive() {
        return isFlying;
    }

    public boolean hitObject(Object object) {
        float myX = this.getX(Align.center);
        float myY = this.getY(Align.center);
        if(myX < object.getX() || myX >= object.getX(Align.right))
            return false;
        if(myY < object.getY() || myY >= object.getY(Align.top))
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
}

package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.utils.Align;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.random;

public class Unit extends Object{
    static final float velocity = 100;
    private TextureAtlas textureAtlas;
    private Animation<TextureRegion> animation;
    private float elapsedTime = 0;
    private boolean unitMoving = false;
    private boolean changeTarget = false;


    public Unit(String unitType, int playerId) {
        super("units/"+unitType+"/"+unitType, playerId);
        type = unitType;
        textureAtlas = new TextureAtlas(Gdx.files.internal("units/"+type+"/"+type+String.valueOf(playerId)+".atlas"));
        animation = new Animation<TextureRegion>(1/8f, textureAtlas.getRegions());
        healthbar.setWidth(getWidth());
        cost = 300;
        reward = 200;
    }

    public void goToPosition(Vector3 pos) {
        if(changeTarget) {
            float dx = ((float)random()-0.5f)*getWidth();
            float dy = ((float)random()-0.5f)*getHeight();
            clearActions();
            MoveToAction moveAction = new MoveToAction();
            moveAction.setPosition(pos.x+dx, pos.y+dy, Align.center);
            float travelTime = distance(pos) / velocity;
            moveAction.setDuration(travelTime);
            RunnableAction completionAction = new RunnableAction(){
                public void run(){
                    unitMoving = false;
                }
            };
            addAction(sequence(moveAction, completionAction));
            unitMoving = true;
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(unitMoving) {
            elapsedTime += Gdx.graphics.getDeltaTime();
            batch.draw(animation.getKeyFrame(elapsedTime, true), getX(), getY());
        }
        else {
            elapsedTime = 0;
            batch.draw(animation.getKeyFrame(elapsedTime, true), getX(), getY());
        }
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }

    public void dispose() {
        textureAtlas.dispose();
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }
}

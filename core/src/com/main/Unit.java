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
    static final float velocity = 150;
    private TextureAtlas textureAtlas;
    private Animation<TextureRegion> animation;
    private float elapsedTime = 0;
    private boolean unitMoving = false;
    private boolean changeTarget = false;


    public Unit(String unitType, int color) {
        super("units/"+unitType+"/"+unitType, color);
        type = unitType;
        textureAtlas = new TextureAtlas(Gdx.files.internal("units/"+type+"/"+type+String.valueOf(color)+".atlas"));
        animation = new Animation<TextureRegion>(1/8f, textureAtlas.getRegions());
        healthbar.setWidth(this.getWidth());
    }

    public void goToPosition(Vector3 pos) {
        if(changeTarget) {
            float dx = ((float)random()-0.5f)*this.getWidth();
            float dy = ((float)random()-0.5f)*this.getHeight();
            this.clearActions();
            MoveToAction moveAction = new MoveToAction();
            moveAction.setPosition(pos.x+dx, pos.y+dy, Align.center);
            float travelTime = this.distance(pos) / velocity;
            moveAction.setDuration(travelTime);
            RunnableAction completionAction = new RunnableAction(){
                public void run(){
                    unitMoving = false;
                }
            };
            this.addAction(sequence(moveAction, completionAction));
            unitMoving = true;
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(unitMoving) {
            elapsedTime += Gdx.graphics.getDeltaTime();
            batch.draw(animation.getKeyFrame(elapsedTime, true), this.getX(), this.getY());
        }
        else {
            elapsedTime = 0;
            batch.draw(animation.getKeyFrame(elapsedTime, true), this.getX(), this.getY());
        }
    }

    public void dispose() {
        textureAtlas.dispose();
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }
}

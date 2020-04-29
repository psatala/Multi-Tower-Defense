package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Align;

import java.util.Vector;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.random;

public class Unit extends Object{
    static final float velocity = 100;
    private TextureAtlas textureAtlas;
    private Animation<TextureRegion> animation;
    private GameManager gameManager;
    private float elapsedTime = 0;
    private boolean unitMoving = false;
    private boolean changeTarget = false;
    private float mapW;
    private float mapH;
    private Vector3 currentTarget;


    public Unit(String unitType, int playerId, GameManager gameManager) {
        super("units/"+unitType+"/"+unitType, playerId);
        this.gameManager = gameManager;
        mapW = gameManager.getMapWidth();
        mapH = gameManager.getMapHeight();
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
            pos.x += dx;
            pos.x = min(mapW-1, max(0, pos.x));
            pos.y += dy;
            pos.y = min(mapH-1, max(0, pos.y));

            currentTarget = new Vector3(pos);
            Vector<Vector3> waypoints = gameManager.findPath(getCenter(), pos);
            SequenceAction seq = new SequenceAction();
            clearActions();
            MoveToAction moveAction;
            float travelTime;
            Vector3 prev = getCenter();
            for(Vector3 waypoint : waypoints) {
                moveAction = new MoveToAction();
                moveAction.setPosition(waypoint.x, waypoint.y, Align.center);
                travelTime = distance(waypoint, prev) / velocity;
                prev = waypoint;
                moveAction.setDuration(travelTime);
                seq.addAction(moveAction);
            }
            RunnableAction completionAction = new RunnableAction(){
                public void run(){
                    unitMoving = false;
                }
            };
            seq.addAction(completionAction);
            addAction(seq);
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

    public Vector3 getCenter() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }
}

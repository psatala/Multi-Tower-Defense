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

public class Unit extends Entity {
    private float velocity;
    private TextureAtlas textureAtlas;
    private Animation<TextureRegion> animation;
    private MapActor map;
    private float elapsedTime = 0;
    private boolean unitMoving = false;
    private boolean changeTarget = false;
    private float mapW;
    private float mapH;
    private Vector3 currentTarget;


    public Unit(String unitType, int playerId, MapActor map, boolean drawable) {
        super(unitType, playerId, drawable);
        this.map = map;
        entityType = Type.UNIT;
        mapW = map.getWidth();
        mapH = map.getHeight();
        if(isDrawable) {
            textureAtlas = new TextureAtlas(Gdx.files.internal(Config.fullTexture.get(type)+String.valueOf(playerId)+".atlas"));
            animation = new Animation<TextureRegion>(1/8f, textureAtlas.getRegions());
        }
        healthbar.setWidth(getWidth());
        currentTarget = new Vector3(getCenter());
        velocity = Config.speed.get(type);
    }

    public Unit(String stateString, MapActor map, boolean drawable) {
        super(stateString, drawable);
        String[] data = stateString.split(" ");
        currentTarget = new Vector3();
        currentTarget.x = Float.parseFloat(data[8]);
        currentTarget.y = Float.parseFloat(data[9]);

        this.map = map;
        entityType = Type.UNIT;
        mapW = map.getWidth();
        mapH = map.getHeight();
        if(isDrawable) {
            textureAtlas = new TextureAtlas(Gdx.files.internal(Config.fullTexture.get(type)+String.valueOf(playerId)+".atlas"));
            animation = new Animation<TextureRegion>(1/8f, textureAtlas.getRegions());
        }
        healthbar.setWidth(getWidth());
        velocity = Config.speed.get(type);
        reconsiderMovement();
    }

    @Override
    public void setState(String stateString) {
        super.setState(stateString);
        String[] data = stateString.split(" ");
        Vector3 setTarget = new Vector3();
        setTarget.x = Float.parseFloat(data[8]);
        setTarget.y = Float.parseFloat(data[9]);
        if(!equalsTarget(setTarget)){
            currentTarget = setTarget;
            reconsiderMovement();
        }
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s.substring(1, s.length()-1);
        s = "U" + s;
        s += currentTarget.x + " ";
        s += currentTarget.y + " ";
        s += "\n";
        return s;
    }

    public void goToPosition(Vector3 pos) {
        if(changeTarget && !equalsTarget(pos)) {
            float dx = ((float)random()-0.5f)*getWidth()/2;
            float dy = ((float)random()-0.5f)*getHeight()/2;
            pos.x += dx;
            pos.x = min(mapW-1, max(0, pos.x));
            pos.y += dy;
            pos.y = min(mapH-1, max(0, pos.y));
            currentTarget = new Vector3(pos);
        }
        reconsiderMovement();
    }

    public void reconsiderMovement() {
        if(!targetAchieved()) {
            Vector<Vector3> waypoints = map.findPath(getCenter(), currentTarget);
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
        if(!isDrawable)
            return;
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

    @Override
    public void setPosition(float x, float y, int align) {
        super.setPosition(x, y, align);
        if(!unitMoving) {
            currentTarget = getCenter();
        }
    }

    public void dispose() {
        textureAtlas.dispose();
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }

    public boolean isTargetChangeable() {
        return changeTarget;
    }

    public Vector3 getCenter() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }

    public boolean targetAchieved() {
        Vector3 pos = getCenter();
        return pos.x == currentTarget.x && pos.y == currentTarget.y;
    }

    public boolean equalsTarget(Vector3 pos) {
        return pos.x == currentTarget.x && pos.y == currentTarget.y;
    }

    public Vector3 getCurrentTarget() {
        return currentTarget;
    }
}

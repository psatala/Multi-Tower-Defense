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

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.random;


/**
 * This class extends Entity with attributes and methods controlling movement and animation.
 * @author Piotr Libera
 */
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


    /**
     * Public constructor for Unit
     * @param unitType Name of unit type as defined in the config file
     * @param playerId ID of this unit's owner
     * @param map Reference to the map actor
     * @param drawable True if the unit will be drawn (as in player's client). Set to false for main server's simulation
     */
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

    /**
     * Public constructor for creating Unit object from a string representation
     * @param stateString String representation of a unit
     * @param map Reference to the map actor
     * @param drawable True if the unit will be drawn (as in player's client). Set to false for main server's simulation
     */
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

    /**
     * Updates the state of the Unit based on the string representation
     * @param stateString String representation of a unit
     */
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

    /**
     * Creates a string representation
     * @return String representation of a unit
     */
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

    /**
     * Changes the movement target of the Unit.
     * Target can only be channged if the target can be changed (unit was selected by the player; changeTarget is true)
     * @param pos Position to change the target to.
     */
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

    /**
     * Recalculates the path of the movement. If the target cannot be achieved
     * the Unit moves towards an achievable point closest to target as calculated by MapActor.findPath()
     */
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

    /**
     * Overrides Actor's draw method
     * @param batch
     * @param alpha
     */
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

    /**
     * Creates a grid update
     * @return Vector3 in which x and y are the unit's position, and z == 0 means that this position is not empty, but also not blocked
     */
    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }

    /**
     * Moves the unit to a given position immediately (and sets its target to this position)
     * @param x X coordinate of a target position
     * @param y Y coordinate of a target position
     * @param align alignment as defined by libgdx's Align
     */
    @Override
    public void setPosition(float x, float y, int align) {
        super.setPosition(x, y, align);
        if(!unitMoving) {
            currentTarget = getCenter();
        }
    }

    /**
     * Disposes libgdx objects
     */
    public void dispose() {
        textureAtlas.dispose();
    }

    /**
     * Allows or forbids the Unit to change its target according to the value of the parameter
     * @param x True - allow target changing; False - Forbid target changing
     */
    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }

    /**
     * Getter method
     * @return Boolean that says if the target is changeable
     */
    public boolean isTargetChangeable() {
        return changeTarget;
    }

    /**
     * Getter method
     * @return Position of the center of the Unit with z = 0
     */
    public Vector3 getCenter() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }

    /**
     * Checks if the target was achieved (position of the center is exactly on target)
     * @return True if target is achieved, False otherwise
     */
    public boolean targetAchieved() {
        Vector3 pos = getCenter();
        return pos.x == currentTarget.x && pos.y == currentTarget.y;
    }

    /**
     * Checks if a given position is equal to unit's current target
     * @param pos Position to check against
     * @return True if pos and current target are equal, False otherwise
     */
    public boolean equalsTarget(Vector3 pos) {
        return pos.x == currentTarget.x && pos.y == currentTarget.y;
    }

    /**
     * Getter method
     * @return Current target of the unit
     */
    public Vector3 getCurrentTarget() {
        return currentTarget;
    }
}

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


/**
 * This class represents missiles - objects that start moving straight to the target (certain position on the map) and carry damage.<br>
 * Once it achieves its target it stops there. The game manager is responsible for checking (using the missile's method)
 * if the missile has hit an entity, or if the missile has achieved its target and needs to be removed.
 * @author Piotr Libera
 */
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
    private boolean isDrawable;

    /**
     * Public constructor of Missile
     * @param target Target entity, it is used to extract the position of the target (missile will not follow this entity)
     * @param source Shooting entity providing necessary parameters such as damage points and starting position
     * @param type Type of the missile as defined in the config file
     * @param drawable <code>true</code> if the missile will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     */
    public Missile(Entity target, Entity source, String type, boolean drawable) {
        isDrawable = drawable;
        this.type = type;
        id = idCounter;
        idCounter++;
        if(isDrawable) {
            texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
            setBounds(0, 0, texture.getWidth(), texture.getHeight());
        }
        else {
            setBounds(0, 0, Config.width.get(type), Config.height.get(type));
        }
        velocity = Config.speed.get(type);
        this.target = new Vector3(target.getX(Align.center), target.getY(Align.center), 0);
        damage = source.getDamage();
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

    /**
     * Public constructor for creating Missile object from the string representation
     * @param stateString String representation of a missile
     * @param drawable <code>true</code> if the missile will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     * @see Missile#toString()
     */
    public Missile(String stateString, boolean drawable) {
        isDrawable = drawable;
        String[] data = stateString.split(" ");
        id = Integer.parseInt(data[1]);
        type = data[2];
        playerId = Integer.parseInt(data[3]);
        target = new Vector3(Float.parseFloat(data[6]), Float.parseFloat(data[7]), 0);
        damage = Float.parseFloat(data[8]);
        if(isDrawable) {
            texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
            setBounds(0, 0, texture.getWidth(), texture.getHeight());
        }
        else {
            setBounds(0, 0, Config.width.get(type), Config.height.get(type));
        }
        velocity = Config.speed.get(type);
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

    /**
     * Updates the state of the missile based on a string representation
     * @param stateString String representation of a missile
     * @see Missile#toString()
     */
    public void setState(String stateString) {
        String[] data = stateString.split(" ");
        setX(Float.parseFloat(data[4]), Align.center);
        setY(Float.parseFloat(data[5]), Align.center);
    }

    /**
     * Creates a string representation of the missile. The string consists of converted attributes seperated by single white spaces in the following order:<br>
     *     - char c - a first single char representing the type of an object, in this case 'M'<br>
     *     - int id - ID of the missile<br>
     *     - String type - type of the missile as defined in the config file<br>
     *     - int playerId - ID of the player owning the missile<br>
     *     - float x - x coordinate of the center of the missile<br>
     *     - float y - y coordinate of the center of the missile<br>
     *     - float targetX - x coordinate of the target<br>
     *     - float targetY - y coordinate of the target<br>
     *     - float damage - amount of damage the missile deals when it hits an entity
     * @return The string representation of the missile
     */
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

    /**
     * Getter method
     * @return The target of the missile
     */
    public Vector3 getTarget() {
        return target;
    }

    /**
     * Overrides Actor's draw() method
     * @param batch
     * @param alpha
     */
    @Override
    public void draw(Batch batch, float alpha) {
        if(isDrawable)
            batch.draw(texture, getX(), getY());
    }

    /**
     * Checks if the missile is still "flying"; if it hasn't achieved its target yet.<br>
     * A missile that has achieved its target position, or has hit an entity, is no longer needed and should be removed (is "dead" analogously to towers and units).
     * @return
     */
    public boolean isAlive() {
        return isFlying;
    }

    /**
     * Checks if the missile is currently in the area occupied by the given entity, thus hitting it
     * @param entity The given entity
     * @return <code>true</code> if positions of the missile and the entity overlap; <code>false</code> otherwise
     */
    public boolean hitObject(Entity entity) {
        float myX = getX(Align.center);
        float myY = getY(Align.center);
        if(myX < entity.getX() || myX >= entity.getX(Align.right))
            return false;
        if(myY < entity.getY() || myY >= entity.getY(Align.top))
            return false;
        return true;
    }

    /**
     * Called after the missile hits an entity
     */
    public void targetHit() {
        isFlying = false;
    }

    /**
     * Getter method
     * @return damage dealt by this missile
     */
    public float getDamage(){
        return damage;
    }

    /**
     * Getter method
     * @return ID of the player owning this missile
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Getter method
     * @return ID of this missile
     */
    public int getId() {
        return id;
    }

    /**
     * Getter method
     * @param align Alignment as defined by libgdx Align
     * @return Position of the missile with the given alignment
     */
    public Vector3 getPosition(int align) {
        Vector3 pos = new Vector3();
        pos.x = getX(align);
        pos.y = getY(align);
        return pos;
    }

    /**
     * Getter method
     * @return Type of the missile (as defined in the config file
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method
     * @param id Value to set the missile's ID
     */
    public void setId(int id) {
        this.id = id;
    }
}

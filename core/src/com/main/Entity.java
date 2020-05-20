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

/**
 * This class represents objects on the map that have HP and can deal damage - Towers and Units that extend this class.
 * It contains attributes and methods for calculating everything regarding HP, shooting and damage, cost and reward.
 * @author Piotr Libera
 */
public class Entity extends Actor {
    public enum Type{UNIT, TOWER};
    static int idCounter = 0;

    public Type entityType;
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
    protected boolean isDrawable;


    /**
     * Public constructor of Entity
     * @param type Name of entity type as defined in the config file
     * @param playerId ID of this Entity's owner
     * @param drawable True if the entity will be drawn (as in player's client). Set to false for main server's simulation
     */
    public Entity(String type, int playerId, boolean drawable) {
        isDrawable = drawable;
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.representativeTexture.get(type))));
            setBounds(0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        }
        else {
            setBounds(0, 0, Config.width.get(type), Config.height.get(type));
        }

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

        healthbar = new Healthbar(Config.hp.get(type), getWidth(), isDrawable);
        healthbar.setPosition(0, getHeight());
        objectGroup = new Group();
        objectGroup.addActor(this);
        objectGroup.addActor(healthbar);
    }

    /**
     * Public constructor for creating Entity object from a string representation
     * @param stateString String representation of an entity
     * @param drawable True if the entity will be drawn (as in player's client). Set to false for main server's simulation
     */
    public Entity(String stateString, boolean drawable) {
        isDrawable = drawable;
        String[] data = stateString.split(" ");
        id = Integer.parseInt(data[1]);
        type = data[2];
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.representativeTexture.get(type))));
            setBounds(0, 0, textureRegion.getRegionWidth(), textureRegion.getRegionHeight());
        }
        else {
            setBounds(0, 0, Config.width.get(type), Config.height.get(type));
        }

        healthbar = new Healthbar(Config.hp.get(type), getWidth(), isDrawable);
        healthbar.setPosition(0, getHeight());
        objectGroup = new Group();
        objectGroup.addActor(this);
        objectGroup.addActor(healthbar);
        setHP(Float.parseFloat(data[7]));

        playerId = Integer.parseInt(data[3]);
        setX(Float.parseFloat(data[4]), Align.center);
        setY(Float.parseFloat(data[5]), Align.center);
        healthbar.setPosition(getX(), getY(Align.top));
        reloadTimeLeft = Float.parseFloat(data[6]);

        reloadTime = Config.reloadTime.get(type);
        range = Config.range.get(type);
        damage = Config.damage.get(type);
        cost = Config.objectCost.get(type);
        reward = Config.objectReward.get(type);
    }

    /**
     * Updates the state of the Entity based on the string representation
     * @param stateString String representation of an entity
     */
    public void setState(String stateString) {
        String[] data = stateString.split(" ");
        setX(Float.parseFloat(data[4]), Align.center);
        setY(Float.parseFloat(data[5]), Align.center);
        healthbar.setPosition(getX(), getY(Align.top));
        reloadTimeLeft = Float.parseFloat(data[6]);
        setHP(Float.parseFloat(data[7]));
    }

    /**
     * Creates a string representation of an entity. The string consists of converted attributes separated by single white spaces in the following order:
     * - char c - a first single char represents the type of an object - 'E' for Entity, 'U' for Unit, 'T' for Tower
     * - int id - id of this object
     * - String type - the type of this entity as defined in the config file
     * - int playerId - id of the player owning this entity
     * - float x - x coordinate of the center
     * - float y - y coordinate of the center
     * - float reloadTimeLeft - time that needs to pass before taking the next shot
     * - float hp - cuurent hp
     * @return String representation of an entity
     */
    public String toString() {
        String s = "E ";
        s += id + " ";
        s += type + " ";
        s += playerId + " ";
        s += getX(Align.center) + " ";
        s += getY(Align.center) + " ";
        s += reloadTimeLeft + " ";
        s += getHP() + " ";
        s += "\n";
        return s;
    }

    /**
     * Creates a grid update
     * @return Vector3 in which x and y are the entity's position, and z == 0 means that this position is not empty, but also not blocked.
     */
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }

    /**
     * Getter method
     * @return The cost of placing this Entity on the map
     */
    public int getCost() {
        return cost;
    }

    /**
     * Getter method
     * @return The reward for killing this Entity
     */
    public int getReward() {
        return reward;
    }

    /**
     * Overrides Actor's draw method
     * @param batch
     * @param parentAlpha
     */
    @Override
    public void draw(Batch batch, float parentAlpha) {
        if(isDrawable)
            batch.draw(textureRegion, getX(), getY());
    }

    /**
     * Deals damage to the Entity
     * @param healthPoints Amount of HP taken
     * @return <code>true</code> If the entity was killed by that damage (HP <= 0 after dealing damage);
     *         <code>false</code> otherwise.
     */
    public boolean damage(float healthPoints) {
        return healthbar.damage(healthPoints);
    }

    /**
     * Updates the entity's reload time left until a next shot can be taken.
     * @param deltaTime Time that passed since the last call of this method
     */
    public void update(float deltaTime) {
        reloadTimeLeft -= deltaTime;
        reloadTimeLeft = max(0, reloadTimeLeft);
    }

    /**
     * Checks if this entity can take a shot (necessary reload time has passed).
     * If it has, it sets the reload time left to a full value of reload time.
     * @return <code>true</code> If a shot can be taken at the moment;
     *         <code>false</code> otherwise.
     */
    public boolean shoot() {
        if(reloadTimeLeft == 0) {
            reloadTimeLeft = reloadTime;
            return true;
        }
        return false;
    }

    /**
     * Checks if entity is alive
     * @return <code>true</code> If HP > 0;
     *         <code>false</code> otherwise.
     */
    public boolean isAlive() {
        return healthbar.getHP() > 0;
    }


    /**
     * Calculates distance from the center of this entity to the given position
     * @param pos Position to calculate the distance to
     * @return The distance between the center and the given position
     */
    public float distance(Vector3 pos) {
        return (float)sqrt(pow(getX(Align.center)-pos.x, 2) + pow(getY(Align.center)-pos.y, 2));
    }

    /**
     * Calculates the distance between two given positions
     * @param a First position
     * @param b Second position
     * @return The distance between a and b
     */
    static public float distance(Vector3 a, Vector3 b) {
        return (float)sqrt(pow(a.x-b.x, 2) + pow(a.y-b.y, 2));
    }

    /**
     * Calculates the distance from the center of this entity to the center of the given entity
     * @param entity Another entity to calculate the distance to
     * @return The distance between this entity and the given entity
     */
    public float distance(Entity entity) {
        return (float)sqrt(pow(getX(Align.center)- entity.getX(Align.center), 2) + pow(getY(Align.center)- entity.getY(Align.center), 2));
    }

    /**
     * Moves the entity to a given position immediately
     * @param x X coordinate of a target position
     * @param y Y coordinate of a target position
     * @param align alignment as defined by libgdx's Align
     */
    @Override
    public void setPosition(float x, float y, int align) {
        healthbar.setPosition(x, y+getHeight()/2, align);
        super.setPosition(x, y, align);
    }

    /**
     * Overrides Actor's remove() method and calls the remove() method on the Group object representing this entity
     * @return The value returned by remove() called on the Group object
     */
    @Override
    public boolean remove() {
        return objectGroup.remove();
    }

    /**
     * Getter method
     * @return Group object representing this entity
     */
    public Group getObjectGroup() {
        return objectGroup;
    }

    /**
     * Getter method
     * @return Damage that this entity deals
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Getter method
     * @return Shooting range of this entity
     */
    public float getRange() {
        return range;
    }

    /**
     * Getter method
     * @return ID of the player owning this entity
     */
    public int getPlayerId() {
        return playerId;
    }

    /**
     * Getter method
     * @return ID of this entity
     */
    public int getId() {
        return id;
    }

    /**
     * Getter method
     * @return Time left before another shot can be taken
     */
    public float getReloadTime() {
        return reloadTimeLeft;
    }

    /**
     * Setter method
     * @param t Value to set the current reload time left - the time before another shot can be taken
     */
    public void setReloadTime(float t) {
        reloadTimeLeft = t;
    }

    /**
     * Getter method
     * @return Current HP of this entity
     */
    public float getHP() {
        return healthbar.getHP();
    }

    /**
     * Setter method
     * @param hp value to set the current HP of this entity
     */
    public void setHP(float hp) {
        healthbar.setHP(hp);
    }

    /**
     * Getter method
     * @return The type of this entity (as defined in the config file)
     */
    public String getType() {
        return type;
    }

    /**
     * Setter method
     * @param id Value to set the ID of this entity
     */
    public void setId(int id) {
        this.id = id;
    }
}

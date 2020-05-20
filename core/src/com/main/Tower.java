package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;


/**
 * This class extends Entity with methods specific for Towers
 * @author Piotr Libera
 */
public class Tower extends Entity {

    /**
     * Public constructor of Tower
     * @param type Name of tower type as defined in the config file
     * @param playerId ID of this Tower's owner
     * @param drawable True if the tower will be drawn (as in player's client). Set to false for main server's simulation
     */
    public Tower(String type, int playerId, boolean drawable) {
        super(type, playerId, drawable);
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.fullTexture.get(type)+playerId+".png")));
        }
        entityType = Type.TOWER;
    }

    /**
     * Public constructor for creating Tower object from a string representation
     * @param stateString String representation of a tower
     * @param drawable True if the tower will be drawn (as in player's client). Set to false for main server's simulation
     */
    public Tower(String stateString, boolean drawable) {
        super(stateString, drawable);
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.fullTexture.get(type)+playerId+".png")));
        }
        entityType = Type.TOWER;
    }

    /**
     * Creates a string representation by taking the representation created by Entity.toString() and changing the first char.
     * Tower is represented as 'T' as the first char of the string.
     * @return String representation of a tower
     * @see Entity.toString()
     */
    @Override
    public String toString() {
        String s = super.toString();
        s = s.substring(1);
        s = "T" + s;
        return s;
    }

    /**
     * Creates a grid update
     * @return Vector3 in which x and y are the tower's position, and z == 1 means that this position is blocked
     */
    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 1);
    }
}

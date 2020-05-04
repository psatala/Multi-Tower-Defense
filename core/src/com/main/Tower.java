package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class Tower extends Entity {

    public Tower(String type, int playerId) {
        super(type, playerId);
        entityType = Type.TOWER;
    }

    public Tower(String stateString) {
        super(stateString);
        entityType = Type.TOWER;
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s.substring(1, s.length()-1);
        s = "T" + s;
        return s;
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 1);
    }
}
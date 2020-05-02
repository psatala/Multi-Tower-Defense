package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class Tower extends Entity {

    public Tower(String type, int playerId) {
        super(type, playerId);
        entityType = Type.TOWER;
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 1);
    }
}

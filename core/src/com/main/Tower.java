package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class Tower extends Object {

    public Tower(String type, int playerId) {
        super("towers/"+type+"/"+type, playerId);
        cost = 400;
        reward = 100;
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 1);
    }
}

package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class Tower extends Object {

    public Tower(String type, int color) {
        super("towers/"+type+"/"+type, color);
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(this.getX(Align.center), this.getY(Align.center), 1);
    }
}

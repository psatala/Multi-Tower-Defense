package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;

public class Tower extends Entity {

    public Tower(String type, int playerId, boolean drawable) {
        super(type, playerId, drawable);
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.fullTexture.get(type)+playerId+".png")));
        }
        entityType = Type.TOWER;
    }

    public Tower(String stateString, boolean drawable) {
        super(stateString, drawable);
        if(isDrawable) {
            textureRegion = new TextureRegion(new Texture(Gdx.files.internal(Config.fullTexture.get(type)+playerId+".png")));
        }
        entityType = Type.TOWER;
    }

    @Override
    public String toString() {
        String s = super.toString();
        s = s.substring(1);
        s = "T" + s;
        return s;
    }

    @Override
    public Vector3 gridUpdate() {
        return new Vector3(getX(Align.center), getY(Align.center), 1);
    }
}

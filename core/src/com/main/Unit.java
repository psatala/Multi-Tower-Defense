package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

public class Unit extends Object{
    private TextureAtlas textureAtlas;
    private Animation<TextureRegion> animation;
    private float elapsedTime = 0;
    private Vector3 targetPosition;
    private boolean unitMoving = false;
    private boolean changeTarget = true;
    private String type;


    public Unit(float x, float y, String unitType) {
        super(x, y);
        type = unitType;
        textureAtlas = new TextureAtlas(Gdx.files.internal("units/"+type+"/"+type+".atlas"));
        animation = new Animation<TextureRegion>(1/8f, textureAtlas.getRegions());
        //width = textureAtlas.findRegion(type+"0").getTexture().getWidth();
        //height = textureAtlas.findRegion(type+"0").getTexture().getHeight();
        width = 64;
        height = 64;
        System.out.println(String.valueOf(width)+"  "+String.valueOf(height));
        targetPosition = new Vector3(position);
    }

    public void setTarget(Vector3 pos) {
        if(changeTarget) {
            float dx = (float)random()*width - width/2;
            float dy = (float)random()*height - height/2;
            targetPosition.set(pos.x+dx, pos.y+dy, 0);
        }
    }

    public void update() {
        moveToTarget(150);
    }

    public void draw(SpriteBatch batch) {
        if(unitMoving) {
            elapsedTime += Gdx.graphics.getDeltaTime();
            batch.draw(animation.getKeyFrame(elapsedTime, true), position.x-width/2, position.y-height/2);
        }
        else {
            elapsedTime = 0;
            batch.draw(animation.getKeyFrame(elapsedTime, true), position.x-width/2, position.y-height/2);
        }

    }

    public void dispose() {
        textureAtlas.dispose();
    }

    public void moveToTarget(int velocity) {
        float deltaDist = velocity*Gdx.graphics.getDeltaTime();
        float distX = abs(targetPosition.x - position.x);
        float distY = abs(targetPosition.y - position.y);
        if(distX*distX + distY*distY > deltaDist*deltaDist) {
            unitMoving = true;
            float dx, dy;
            if(distY == 0){
                dy = 0;
                dx = deltaDist;
            }
            else {
                dy = (float)(deltaDist/sqrt(1+pow(distX/distY, 2)));
                dx = dy*distX/distY;
            }
            if(position.x < targetPosition.x)
                position.x += dx;
            else
                position.x -= dx;
            if(position.y < targetPosition.y)
                position.y += dy;
            else
                position.y -= dy;
        }
        else {
            unitMoving = false;
            position.set(targetPosition);
        }
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }
}

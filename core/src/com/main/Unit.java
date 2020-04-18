package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

public class Unit extends Object{
    private String currentAtlasKey;
    private float updateFreq = 8.0f;

    private TextureAtlas textureAtlas;
    private int currentFrame = 0;
    private Sprite sprite;
    private Vector3 targetPosition;
    private boolean unitMoving = false;
    private boolean changeTarget = false;
    private String type;


    public Unit(float x, float y, String unitType) {
        super(x, y);
        type = unitType;
        textureAtlas = new TextureAtlas(Gdx.files.internal("units/"+type+"/"+type+".atlas"));
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion(type+"0");
        sprite = new Sprite(region);
        width = sprite.getWidth();
        height = sprite.getHeight();
        sprite.setPosition(position.x-width/2, position.y - height/2);
        targetPosition = new Vector3(position);
        Timer.schedule(new Timer.Task(){
                           @Override
                           public void run() {
                               if(unitMoving)
                                   currentFrame++;
                               else
                                   currentFrame = 0;
                               if(currentFrame >= 4)
                                   currentFrame = 0;

                               currentAtlasKey = type+String.format("%d", currentFrame);
                               sprite.setRegion(textureAtlas.findRegion(currentAtlasKey));
                           }
                       }
                ,0,1/updateFreq);
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
        sprite.draw(batch);
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
        sprite.setPosition(position.x-width/2, position.y-height/2);
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
    }
}

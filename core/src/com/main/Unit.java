package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.random;
import static java.lang.Math.sqrt;

public class Unit {
    private String currentAtlasKey = new String("firstUnit0");
    private float updateFreq = 8.0f;

    private Camera mCamera;
    private TextureAtlas textureAtlas;
    private int currentFrame = 0;
    private Sprite atlasSprite;
    private Texture targetTexture;
    private Sprite targetSprite;
    private float targetx;
    private float targety;
    private boolean unitMoving = false;
    private boolean targetProcessed = false;
    private boolean changeTarget = true;

    public Unit(float x, float y, Camera cam) {
        mCamera = cam;

        targetTexture = new Texture(Gdx.files.internal("plus.png"));
        targetSprite = new Sprite(targetTexture);

        textureAtlas = new TextureAtlas(Gdx.files.internal("units/firstUnit/firstUnit.atlas"));
        TextureAtlas.AtlasRegion region = textureAtlas.findRegion("firstUnit0");
        atlasSprite = new Sprite(region);
        atlasSprite.setPosition(x, y);
        targetx = x;
        targety = y;
        Timer.schedule(new Timer.Task(){
                           @Override
                           public void run() {
                               if(unitMoving)
                                   currentFrame++;
                               else
                                   currentFrame = 0;
                               if(currentFrame >= 4)
                                   currentFrame = 0;

                               currentAtlasKey = String.format("firstUnit%d", currentFrame);
                               atlasSprite.setRegion(textureAtlas.findRegion(currentAtlasKey));
                           }
                       }
                ,0,1/updateFreq);
    }

    public void update() {
        if(changeTarget && Gdx.input.isTouched()) {
            if(!targetProcessed) {
                Vector3 touchPos = new Vector3();
                touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
                mCamera.unproject(touchPos);
                targetx = touchPos.x-32 + (float)random()*64 - 32;
                targety = touchPos.y-32 + (float)random()*64 - 32;
                targetProcessed = true;
            }
        }
        else
            targetProcessed = false;

        moveToTarget(150);
    }

    public void draw(SpriteBatch batch) {
        targetSprite.setPosition(targetx, targety);
        targetSprite.draw(batch);
        atlasSprite.draw(batch);
    }

    public void dispose() {
        targetTexture.dispose();
        textureAtlas.dispose();
    }

    public void moveToTarget(int velocity) {
        float deltaDist = velocity*Gdx.graphics.getDeltaTime();
        float distX = abs(targetx - atlasSprite.getX());
        float distY = abs(targety - atlasSprite.getY());
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
            if(atlasSprite.getX() < targetx)
                atlasSprite.setX(atlasSprite.getX() + dx);
            else
                atlasSprite.setX(atlasSprite.getX() - dx);
            if(atlasSprite.getY() < targety)
                atlasSprite.setY(atlasSprite.getY() + dy);
            else
                atlasSprite.setY(atlasSprite.getY() - dy);
        }
        else {
            unitMoving = false;
            atlasSprite.setPosition(targetx, targety);
        }
    }

    public void allowTargetChanging(boolean x) {
        changeTarget = x;
        System.out.println(x);
    }

    public float getX() {
        return atlasSprite.getX();
    }

    public float getY() {
        return atlasSprite.getY();
    }
}

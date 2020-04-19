package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Missile {
    private Object target;
    private float damage;
    private Vector3 position;
    private Texture texture;
    private Sprite sprite;
    private float width;
    private float height;

    public Missile(Object target, Object source) {
        this.target = target;
        damage = source.damage;
        position = new Vector3(source.getPosition());
        texture = new Texture(Gdx.files.internal("missile.png"));
        sprite = new Sprite(texture);
        width = sprite.getWidth();
        height = sprite.getHeight();
        sprite.setPosition(position.x-width/2, position.y-height/2);
    }

    public boolean update() {
        boolean ret = moveToTarget(300);
        sprite.setPosition(position.x-width/2, position.y-height/2);
        return ret;
    }

    public Object getTarget() {
        return target;
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public boolean moveToTarget(int velocity) {
        Vector3 targetPosition = target.getPosition();
        float deltaDist = velocity* Gdx.graphics.getDeltaTime();
        float distX = abs(targetPosition.x - position.x);
        float distY = abs(targetPosition.y - position.y);
        if(distX*distX + distY*distY > deltaDist*deltaDist) {
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
            return false;
        }
        else {
            target.damage(damage);
            return true;
        }
    }
}

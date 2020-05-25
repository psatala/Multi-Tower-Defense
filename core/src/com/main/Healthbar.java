package com.main;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * This class contains information about current and max HP of an Entity. It is also responsible
 * for drawing a healthbar above each entity.
 * @author Piotr Libera
 */
public class Healthbar extends Actor {
    private final float height = 5;
    private Pixmap greenPartPixmap;
    private Texture greenPart;
    private Pixmap redPartPixmap;
    private Texture redPart;
    private float hp;
    private float maxHP;
    private boolean isDrawable;

    /**
     * Public constructor of Healthbar
     * @param maximumHP maximum (and initial) HP
     * @param w width of the healthbar
     * @param drawable <code>true</code> if the healthbar will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     */
    public Healthbar(float maximumHP, float w, boolean drawable) {
        setBounds(0, 0, w, height);
        isDrawable = drawable;
        maxHP = maximumHP;
        hp = maxHP;
        if(isDrawable)
            createTexture();
    }

    /**
     * Creates the healtbar texture, consisting of two rectangles: green and red<br>
     *     The width of the green rectangle is proportional to the current HP,
     *     and the total width of both rectangles is proportional to maxHP.
     */
    private void createTexture() {
        greenPartPixmap = new Pixmap((int)height, (int)height, Pixmap.Format.RGBA8888);
        redPartPixmap = new Pixmap((int)height, (int)height, Pixmap.Format.RGBA8888);
        greenPartPixmap.setColor(Color.GREEN);
        greenPartPixmap.fillRectangle(0, 0, (int)height, (int)height);
        greenPart = new Texture(greenPartPixmap);
        redPartPixmap.setColor(Color.RED);
        redPartPixmap.fillRectangle(0, 0, (int)height, (int)height);
        redPart = new Texture(redPartPixmap);
        greenPartPixmap.dispose();
        redPartPixmap.dispose();
    }

    /**
     * Subtracts health points from the current HP
     * @param healthPoints Number of health points to subtract
     * @return <code>true</code> if the entity was killed (HP {@literal <}= 0), <code>false</code> otherwise
     */
    public boolean damage(float healthPoints) {
        hp -= healthPoints;
        if(hp <= 0) {
            hp = 0;
            return true;
        }
        return false;
    }

    /**
     * Overrides Actor's draw() method
     * @param batch
     * @param alpha
     */
    @Override
    public void draw(Batch batch, float alpha) {
        if(isDrawable) {
            batch.draw(greenPart, getX(), getY(), getWidth()*(hp/maxHP), getHeight());
            batch.draw(redPart, getX()+getWidth()*(hp/maxHP), getY(), getWidth()*(1-hp/maxHP), getHeight());
        }
    }

    /**
     * Getter method
     * @return Current HP
     */
    public float getHP() {
        return hp;
    }

    /**
     * Setter method
     * @param hp Value to set current HP
     */
    public void setHP(float hp) {
        this.hp = hp;
    }
}

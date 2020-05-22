package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;


/**
 * This class represents a single grid cell on the map. It contains information if the given cell is empty or blocked,
 * as well as highlights the grid cell if the mouse hovers above it in BUILD or SPAWN modes.
 * @author Piotr Libera
 * @see MapActor.Mode
 */
public class GridCell extends Actor {
    private boolean isEmpty = true;
    private boolean isBlocked = false;
    private boolean isHighlighted = false;
    private MapActor map;
    private ShapeRenderer renderer;
    private boolean isDrawable;

    /**
     * Public constructor of the grid cell
     * @param x X coordinate of the bottom left corner of the grid cell
     * @param y Y coordinate of the bottom left corner of the grid cell
     * @param w Width of the grid cell
     * @param h Height of the grid cell
     * @param m mapActor that created this grid cell - it contains information about currently chosen mode
     * @param drawable <code>true</code> if the map will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     */
    public GridCell(float x, float y, float w, float h, MapActor m, boolean drawable) {
        isDrawable = drawable;
        map = m;
        setBounds(x, y, w, h);
        if(isDrawable) {
            renderer = new ShapeRenderer();
            addListener(createHoverListener());
        }
    }

    /**
     * In BUILD mode: draws the red highlight on the entire cell if it is not empty,
     * and the green highlight if it isn't and is currently hovered on,<br>
     * In SPAWN mode: draws the red highlight on the entire cell if it is blocked,
     * and the green highlight if it isn't and is currently hovered on,<br>
     * If the grid cell is not drawable, or in any other mode, nothing will be drawn.
     * @param batch
     * @param alpha
     */
    @Override
    public void draw(Batch batch, float alpha) {
        if(!isDrawable)
            return;
        if((map.mode == MapActor.Mode.BUILD || map.mode == MapActor.Mode.SPAWN) && (!isEmpty || isHighlighted)) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            if(map.mode == MapActor.Mode.BUILD && !isEmpty || map.mode == MapActor.Mode.SPAWN && isBlocked){
                renderer.setColor(new Color(1.0f, 0.0f, 0.0f, 0.3f));
                renderer.rect(getX(), getY(), getWidth(), getHeight());
            }
            if(isHighlighted){
                renderer.setColor(new Color(0.0f, 1.0f, 0.0f, 0.3f));
                renderer.rect(getX(), getY(), getWidth(), getHeight());
            }
            renderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
    }

    /**
     * Creates the listener that listens for the cursor entering and leaving the grid cell
     * @return Created listener
     */
    private ClickListener createHoverListener() {
        return new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isHighlighted = true;
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                isHighlighted = false;
            }
        };
    }

    /**
     * Checks if the cell is empty
     * @return <code>true</code> if the cell is empty; <code>false</code> otherwise
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * Checks if the cell is blocked
     * @return <code>true</code> if the cell is blocked; <code>false</code> otherwise
     */
    public boolean isBlocked() {
        return isBlocked;
    }

    /**
     * Sets the cell blocked state
     * @param x <code>true</code> to set the blocked state to <code>true</code> (and empty state to <code>false</code>); <code>false</code> to set the blocked state to <code>false</code>
     */
    public void setBlocked(boolean x) {
        isBlocked = x;
        if(isBlocked)
            isEmpty = false;
    }

    /**
     * Sets the cell empty state
     * @param x <code>true</code> to set the empty state to <code>true</code> (and blocked state to <code>false</code>); <code>false</code> to set the empty state to <code>false</code>
     */
    public void setEmpty(boolean x) {
        isEmpty = x;
        if(isEmpty)
            isBlocked = false;
    }

    /**
     * Getter method
     * @return Position of the center of this grid cell
     */
    public Vector3 getCenter() {
        return new Vector3(getX(Align.center), getY(Align.center), 0);
    }
}

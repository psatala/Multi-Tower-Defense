package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MapActor extends Group {
    public enum Mode{MOVE, SELECT, BUILD};
    public static final int gridW = 20;
    public static final int gridH = 10;

    public GridCell[][] gridCells = new GridCell[gridW][gridH];

    private Vector3 selection;
    private Rectangle selectRect;
    private boolean drawSelection = false;
    private float gridCellW;
    private float gridCellH;
    private MainGameView gameManager;
    private ShapeRenderer renderer;
    protected Mode mode;

    public MapActor(float w, float h, MainGameView gameView) {
        gameManager = gameView;
        renderer = new ShapeRenderer();
        this.setBounds(0, 0, w, h);
        gridCellH = h/(float)gridH;
        gridCellW = w/(float)gridW;
        this.addListener(createInterfaceListener());
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y] = new GridCell(x*gridCellW, y*gridCellH, gridCellW, gridCellH, this);
                this.addActor(gridCells[x][y]);
            }
        }
    }

    @Override
    public void draw(Batch batch, float alpha) {
        if(drawSelection) {
            batch.end();
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(new Color(0.5f, 0.5f, 0.5f, 0.5f));
            renderer.rect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
            renderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
            batch.begin();
        }
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y].draw(batch, alpha);
            }
        }
    }

    public void setMode(Mode m) {
        mode = m;
    }

    private InputListener createInterfaceListener() {
        return new InputListener() {
            public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
                selection = new Vector3(x, y, 0);
                if(mode == Mode.SELECT){
                    selectRect = new Rectangle(selection.x, selection.y, 0, 0);
                    drawSelection = true;
                }
                else if(mode == Mode.MOVE){
                    gameManager.sendUnitsTo(selection);
                    drawSelection = false;
                }
                return true;
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if(!drawSelection)
                    return;
                Vector3 dragSelect = new Vector3(x, y, 0);
                selectRect.x = min(selection.x, dragSelect.x);
                selectRect.y = min(selection.y, dragSelect.y);
                selectRect.width = abs(selection.x - dragSelect.x);
                selectRect.height = abs(selection.y - dragSelect.y);
                return;
            }

            public void touchUp (InputEvent event, float x, float y, int pointer, int button) {
                if(!drawSelection)
                    return;
                drawSelection = false;
                gameManager.selectUnits(selectRect);
            }
        };
    }
}
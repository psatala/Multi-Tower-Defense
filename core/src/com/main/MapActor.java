package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;

import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class MapActor extends Actor {
    public enum Mode{MOVE, SELECT, BUILD};
    public static final int gridW = 20;
    public static final int gridH = 10;

    public GridCell[][] gridCells = new GridCell[gridW][gridH];

    private Vector3 selection;
    private Rectangle selectRect;
    private boolean drawSelection = false;
    private float gridCellW;
    private float gridCellH;
    private GameManager gameManager;
    private Group mapGroup;
    private ShapeRenderer renderer;
    protected Mode mode;
    private int playerId;

    public MapActor(float w, float h, GameManager gameManager, int playerId) {
        this.gameManager = gameManager;
        this.playerId = playerId;
        renderer = new ShapeRenderer();
        setBounds(0, 0, w, h);
        gridCellH = h/(float)gridH;
        gridCellW = w/(float)gridW;
        mapGroup = new Group();
        mapGroup.addActor(this);
        mapGroup.addListener(createInterfaceListener());
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y] = new GridCell(x*gridCellW, y*gridCellH, gridCellW, gridCellH, this);
                mapGroup.addActor(gridCells[x][y]);
            }
        }
    }

    public Vector3 getGridCoords(Vector3 mapCoords){
        float x = mapCoords.x/gridCellW;
        float y = mapCoords.y/gridCellH;
        return new Vector3(x, y, 0);
    }

    public Vector3 getGridCoords(float mapX, float mapY){
        float x = mapX/gridCellW;
        float y = mapY/gridCellH;
        return new Vector3(x, y, 0);
    }

    public void updateGrid(Vector<Vector3> updates) {
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y].setEmpty(true);
            }
        }
        Vector3 pos;
        for(Vector3 update : updates) {
            pos = getGridCoords(update);
            if(update.z == 1)
                gridCells[(int)pos.x][(int)pos.y].setBlocked(true);
            else
                gridCells[(int)pos.x][(int)pos.y].setEmpty(false);
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
                else if(mode == Mode.BUILD){
                    Vector3 gridCoords = getGridCoords(x, y);
                    int gx = (int)gridCoords.x;
                    int gy = (int)gridCoords.y;
                    if(gridCells[gx][gy].isEmpty()) {
                        gridCells[gx][gy].setBlocked(true);
                        float tx = gridCells[gx][gy].getX(Align.center);
                        float ty = gridCells[gx][gy].getY(Align.center);
                        gameManager.spawnTower(tx, ty, playerId);
                    }
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

    public Group getMapGroup() {
        return mapGroup;
    }
}
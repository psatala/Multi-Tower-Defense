package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.utils.Align;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.random;

public class MapActor extends Actor {
    public enum Mode{MOVE, SELECT, BUILD, SPAWN};
    private int gridW;
    private int gridH;

    private GridCell[][] gridCells;

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
    private String type;
    private Texture texture;

    public MapActor(float w, float h, GameManager gameManager, int playerId, String type) {
        this.gameManager = gameManager;
        this.playerId = playerId;
        this.type = type;
        texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
        gridW = Config.mapGrid.get(type)[0].length;
        gridH = Config.mapGrid.get(type).length;
        gridCells = new GridCell[gridW][gridH];
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
                gridCells[x][y].setBlocked(Config.mapGrid.get(type)[y][x]);
                gridCells[x][y].setEmpty(!Config.mapGrid.get(type)[y][x]);
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
                gridCells[x][y].setBlocked(Config.mapGrid.get(type)[y][x]);
                gridCells[x][y].setEmpty(!Config.mapGrid.get(type)[y][x]);
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
        batch.draw(texture, getX(), getY(), getWidth(), getHeight());
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
                else if(mode == Mode.SPAWN){
                    Vector3 gridCoords = getGridCoords(x, y);
                    int gx = (int)gridCoords.x;
                    int gy = (int)gridCoords.y;
                    if(!gridCells[gx][gy].isBlocked()) {
                        gridCells[gx][gy].setEmpty(false);
                        gameManager.spawnUnit(x, y, playerId);
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

    public Vector<Vector3> BFS(Vector3 start, Vector3 finish) {
        start = getGridCoords(start);
        Vector3 fv = getGridCoords(finish);
        boolean[][] visited = new boolean[gridW][gridH];
        int[][] distance = new int[gridW][gridH];
        for(int x = 0; x < gridW; ++x){
            for(int y = 0; y < gridH; ++y){
                distance[x][y] = gridW*gridH;
                visited[x][y] = gridCells[x][y].isBlocked();
            }
        }
        int sx = (int)start.x;
        int sy = (int)start.y;
        int fx = (int)fv.x;
        int fy = (int)fv.y;
        Queue<Integer> qx = new LinkedList<>();
        Queue<Integer> qy = new LinkedList<>();
        qx.add(sx);
        qy.add(sy);
        visited[sx][sy] = true;
        distance[sx][sy] = 0;
        while(!qx.isEmpty()) {
            sx = qx.peek();
            sy = qy.peek();
            qx.remove();
            qy.remove();
            if(sx > 0 && !visited[sx-1][sy]){
                qx.add(sx-1);
                qy.add(sy);
                distance[sx-1][sy] = distance[sx][sy]+1;
                visited[sx-1][sy] = true;
            }
            if(sy > 0 && !visited[sx][sy-1]){
                qx.add(sx);
                qy.add(sy-1);
                distance[sx][sy-1] = distance[sx][sy]+1;
                visited[sx][sy-1] = true;
            }
            if(sx < gridW-1 && !visited[sx+1][sy]){
                qx.add(sx+1);
                qy.add(sy);
                distance[sx+1][sy] = distance[sx][sy]+1;
                visited[sx+1][sy] = true;
            }
            if(sy < gridH-1 && !visited[sx][sy+1]){
                qx.add(sx);
                qy.add(sy+1);
                distance[sx][sy+1] = distance[sx][sy]+1;
                visited[sx][sy+1] = true;
            }
            if(distance[fx][fy] < gridW*gridH)
                break;
        }
        Vector<Vector3> waypoints = new Vector<>();
        if(distance[fx][fy] == gridW*gridH) {
            int bestx = sx;
            int besty = sy;
            for(int x = 0; x < gridW; ++x) {
                for(int y = 0; y < gridH; ++y) {
                    if(distance[x][y] != gridW*gridH && pow(fx-x, 2)+pow(fy-y, 2) < pow(bestx-fx, 2)+pow(besty-fy, 2)) {
                        bestx = x;
                        besty = y;
                    }
                }
            }
            fx = bestx;
            fy = besty;

            Vector3 randomizedPosition = gridCells[fx][fy].getCenter();
            randomizedPosition.x += ((float)random()-0.5f)*gridCellW/2;
            randomizedPosition.y += ((float)random()-0.5f)*gridCellH/2;
            waypoints.add(randomizedPosition);
        }
        else {
            waypoints.add(finish);
        }
        waypoints.add(gridCells[fx][fy].getCenter());
        while(distance[fx][fy] > 0) {
            if(fx > 0 && distance[fx-1][fy] == distance[fx][fy]-1){
                fx -= 1;
                waypoints.add(gridCells[fx][fy].getCenter());
            }
            if(fy > 0 && distance[fx][fy-1] == distance[fx][fy]-1){
                fy -= 1;
                waypoints.add(gridCells[fx][fy].getCenter());
            }
            if(fx < gridW-1 && distance[fx+1][fy] == distance[fx][fy]-1){
                fx += 1;
                waypoints.add(gridCells[fx][fy].getCenter());
            }
            if(fy < gridH-1 && distance[fx][fy+1] == distance[fx][fy]-1){
                fy += 1;
                waypoints.add(gridCells[fx][fy].getCenter());
            }
        }
        Collections.reverse(waypoints);
        return waypoints;
    }

    public boolean isLineBlocked(Vector3 start, Vector3 finish) {
        if(start.x == finish.x) {
            start = getGridCoords(start);
            finish = getGridCoords(finish);
            int x = (int)start.x;
            for(int i = (int)min(start.y, finish.y); i <= max(start.y, finish.y); ++i) {
                if(gridCells[x][i].isBlocked())
                    return true;
            }
            return false;
        }
        if(start.x > finish.x){
            Vector3 temp = new Vector3(start);
            start = finish;
            finish = temp;
        }
        Vector3 gridxy = getGridCoords(start);
        int x = (int)gridxy.x;
        int y = (int)gridxy.y;
        gridxy = getGridCoords(finish);
        int fx = (int)gridxy.x;
        int fy = (int)gridxy.y;
        float a = (finish.y-start.y)/(finish.x-start.x);
        float b = finish.y - a * finish.x;
        if(finish.y > start.y){
            while(x != fx || y != fy) {
                if (gridCellH * (y + 1) > a * gridCellW * (x + 1) + b)
                    x += 1;
                else
                    y += 1;
                if (gridCells[x][y].isBlocked())
                    return true;
            }
        }
        else {
            while(x != fx || y != fy) {
                if (gridCellH * y < a * gridCellW * (x + 1) + b)
                    x += 1;
                else
                    y -= 1;
                if (gridCells[x][y].isBlocked())
                    return true;
            }
        }
        return false;
    }

    public Vector<Vector3> smoothPath(Vector<Vector3> waypoints) {
        Vector<Vector3> result = new Vector<>();
        Vector3 prev = new Vector3(waypoints.elementAt(0));
        result.add(prev);
        for(int i = 1; i < waypoints.size(); ++i) {
            if(isLineBlocked(prev, waypoints.elementAt(i))) {
                result.add(waypoints.elementAt(i-1));
                prev = waypoints.elementAt(i-1);
            }
        }
        result.add(waypoints.elementAt(waypoints.size()-1));
        return result;
    }

    public Vector<Vector3> findPath(Vector3 start, Vector3 finish) {
        Vector<Vector3> waypoints = BFS(start, finish);
        waypoints.setElementAt(start, 0);
        waypoints = smoothPath(waypoints);
        return waypoints;
    }
}
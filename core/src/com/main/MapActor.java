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


/**
 * This class covers everything regarding the map. It contains listeners for clicks and replies to them according to currently set Mode.<br>
 *     It also contains the grid - an array comprised of GridCells. It is used to store information about blocked or occupied areas of the map,
 *     as well as to find paths between two given points.
 * @author Piotr Libera
 */
public class MapActor extends Actor {
    /**
     * Mode in which the listeners respond to clicks:<br>
     *     - MOVE - Click on the map sets the clicked point as the target for any of the player's units that can change their target<br>
     *     - SELECT - In this mode the player can drag the cursor on the map to select units that will obtain the ability to change their target.
     *     Any unit outside the selection rectangle will no more be able to change its target.<br>
     *     - BUILD - In this mode the player can choose a grid cell to build a tower in. Tower must be aligned with grid cells, and every grid cell can only contain one tower
     *     (Building the tower blocks the grid cell - no towers can be built and no units can be spawned in a blocked grid cell)<br>
     *     - SPAWN - Click on the map spawns a unit if the chosen position is not inside a blocked grid cell and the player has enough coins to spawn a unit
     */
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
    private boolean isDrawable;

    /**
     * Public constructor of MapActor
     * @param w Width of the map
     * @param h Height of the map
     * @param gameManager gameManager that created this object and manages the game
     * @param playerId ID of the player
     * @param type Type (name) of the map as defined in the config file
     * @param drawable <code>true</code> if the map will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     */
    public MapActor(float w, float h, GameManager gameManager, int playerId, String type, boolean drawable) {
        isDrawable = drawable;
        this.gameManager = gameManager;
        this.playerId = playerId;
        this.type = type;
        mapGroup = new Group();
        mapGroup.addActor(this);
        if(isDrawable) {
            texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
            renderer = new ShapeRenderer();
            mapGroup.addListener(createInterfaceListener());
        }
        gridW = Config.mapGrid.get(type)[0].length;
        gridH = Config.mapGrid.get(type).length;
        gridCells = new GridCell[gridW][gridH];
        setBounds(0, 0, w, h);
        gridCellH = h/(float)gridH;
        gridCellW = w/(float)gridW;
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y] = new GridCell(x*gridCellW, y*gridCellH, gridCellW, gridCellH, this, true);
                mapGroup.addActor(gridCells[x][y]);
                gridCells[x][y].setBlocked(Config.mapGrid.get(type)[y][x]);
                gridCells[x][y].setEmpty(!Config.mapGrid.get(type)[y][x]);
            }
        }
    }

    /**
     * Constructor of MapActor without a player assigned - used by the server.
     * @param w Width of the map
     * @param h Height of the map
     * @param type Type (name) of the map as defined in the config file
     * @param drawable <code>true</code> if the map will be drawn (as in player's client). Set to <code>false</code> for main server's simulation
     */
    public MapActor(float w, float h, String type, boolean drawable) {
        isDrawable = drawable;
        this.type = type;
        mapGroup = new Group();
        mapGroup.addActor(this);
        if(isDrawable) {
            texture = new Texture(Gdx.files.internal(Config.representativeTexture.get(type)));
            renderer = new ShapeRenderer();
            mapGroup.addListener(createInterfaceListener());
        }
        gridW = Config.mapGrid.get(type)[0].length;
        gridH = Config.mapGrid.get(type).length;
        gridCells = new GridCell[gridW][gridH];
        setBounds(0, 0, w, h);
        gridCellH = h/(float)gridH;
        gridCellW = w/(float)gridW;
        for(int x = 0; x < gridW; ++x) {
            for(int y = 0; y < gridH; ++y) {
                gridCells[x][y] = new GridCell(x*gridCellW, y*gridCellH, gridCellW, gridCellH, this, false);
                mapGroup.addActor(gridCells[x][y]);
                gridCells[x][y].setBlocked(Config.mapGrid.get(type)[y][x]);
                gridCells[x][y].setEmpty(!Config.mapGrid.get(type)[y][x]);
            }
        }
    }

    /**
     * Converts any position on the map to the position of the center of the grid cell that this position is in.
     * @param mapCoords A given position, any position on the map
     * @return The position of the center of the grid cell that contains the given position
     */
    public Vector3 getGridCoords(Vector3 mapCoords){
        float x = mapCoords.x/gridCellW;
        float y = mapCoords.y/gridCellH;
        return new Vector3(x, y, 0);
    }

    /**
     * Converts any position on the map to the position of the center of the grid cell that this position is in.
     * @param mapX X coordinate of the given position
     * @param mapY Y coordinate of the given position
     * @return The position of the center of the grid cell that contains the given position
     */
    public Vector3 getGridCoords(float mapX, float mapY){
        float x = mapX/gridCellW;
        float y = mapY/gridCellH;
        return new Vector3(x, y, 0);
    }

    /**
     * Updates empty and blocked values of grid cells based of received state of the map. The received vector needs to be complete
     * as the grid is cleared during every call of this method.
     * @param updates Vector of updates - every update is a Vector3 with 3 coordinates: x and y are the position on the map,
     *                and z is the type of the update:<br>
     *                z == 0 means that an object occupies this position, but does not block it (like a Unit)<br>
     *                z == 1 means that an object blocks this position (like a Tower)
     */
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

    /**
     * Draws the map and possibly the selection rectangle if an area is selected.
     * @param batch
     * @param alpha
     */
    @Override
    public void draw(Batch batch, float alpha) {
        if(!isDrawable)
            return;
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

    /**
     * Setter method
     * @param m Sets the mode of the map
     * @see MapActor.Mode
     */
    public void setMode(Mode m) {
        mode = m;
    }

    /**
     * Creates a listener to respond to clicks on the map according to the selected Mode.
     * @return The created listener
     * @see MapActor.Mode
     */
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
                        gameManager.spawnTower(tx, ty, "firstTower");
                    }
                }
                else if(mode == Mode.SPAWN){
                    Vector3 gridCoords = getGridCoords(x, y);
                    int gx = (int)gridCoords.x;
                    int gy = (int)gridCoords.y;
                    if(!gridCells[gx][gy].isBlocked()) {
                        gridCells[gx][gy].setEmpty(false);
                        gameManager.spawnUnit(x, y, "firstUnit");
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

    /**
     * Getter method
     * @return Group object containing all the actors - the map and every grid cell
     */
    public Group getMapGroup() {
        return mapGroup;
    }

    /**
     * This method performs the BFS algorithm on grid cells. The path cannot pass through blocked cells, but it can pass through occupied cells.
     * The found path consists of waypoints - centers of every grid cell that the path passes through, it must therefore be further processed to achieve smooth movement.<p>
     * If no path exists between two given positions, the returned path is a path between the starting position and the grid cell that is the closest to the finish position
     * and is still achievable. In this case the last waypoint is randomized in the same way and for the same reason as the target in Unit.goToPosition().
     * @param start Starting position - any position on the map, it is transformed to grid cell coordinates inside the method
     * @param finish End position - any position on the map, it is transformed to grid cell coordinates inside the method
     * @return Vector of positions - complete set of waypoints that create the found path
     * @see Unit#goToPosition(Vector3)
     */
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

    /**
     * Checks if the given line segment crosses any blocked grid cells.
     * @param start First end of the line segment
     * @param finish Second end of the line segment
     * @return <code>true</code> if the line segment crosses any blocked grid cells; <code>false</code> otherwise
     */
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

    /**
     * Smooths the given path so that it consists of as few line segments (waypoints) as possible.<br>
     * It is used to smooth the path found by the BFS algorithm. It performs a greedy algorithm on the given waypoints
     * eliminating consecutive waypoints if they are not necessary (the line segment created by their neighbours
     * does not cross any blocked grid cells)
     * @param waypoints Path to smooth out given as a Vector of waypoints
     * @return Smooth path as a Vector of waypoints
     */
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

    /**
     * Finds the path between two given positions.
     * If there is no possible path between these positions
     * the final waypoint is the position that is the closest to the target and still achievable. <br>
     * It performs the BFS algorithm and then smooths the path.
     * @param start Starting position - any position on the map
     * @param finish End position - any position on the map
     * @return Vector containing waypoints of the path
     */
    public Vector<Vector3> findPath(Vector3 start, Vector3 finish) {
        Vector<Vector3> waypoints = BFS(start, finish);
        waypoints.setElementAt(start, 0);
        waypoints = smoothPath(waypoints);
        return waypoints;
    }

    /**
     * Checks if the given position is empty
     * @param x X coordinate of the given position
     * @param y Y coordinate of the given position
     * @return <code>true</code> if the position is in an empty grid cell; <code>false</code> otherwise
     */
    public boolean isPositionEmpty(float x, float y) {
        Vector3 pos = getGridCoords(x, y);
        return gridCells[(int)pos.x][(int)pos.y].isEmpty();
    }

    /**
     * Checks if the given position is blocked
     * @param x X coordinate of the given position
     * @param y Y coordinate of the given position
     * @return <code>true</code> if the position is in a blocked grid cell; <code>false</code> otherwise
     */
    public boolean isPositionBlocked(float x, float y) {
        Vector3 pos = getGridCoords(x, y);
        return gridCells[(int)pos.x][(int)pos.y].isBlocked();
    }
}
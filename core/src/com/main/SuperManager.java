package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;


public class SuperManager extends ApplicationAdapter {
    private Vector<Entity> entities;
    private Vector<Unit> units;
    private Vector<Missile> missiles;
    private MapActor map;
    protected Stage stage;
    private ShapeRenderer renderer;


    public SuperManager() {
        units = new Vector<>();
        entities = new Vector<>();
        missiles = new Vector<>();
    }

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()-InfoActor.topBarHeight, "map0");
        stage.addActor(map.getMapGroup());
        renderer = new ShapeRenderer();

        Timer.schedule(new Timer.Task(){
                           @Override
                           public void run() {
                               updateFight();
                               updateGrid();
                               getUpdates();
                               sendUpdates();
                           }
                       }
                ,0,1/Config.refreshRate);
    }


    private void sendUpdates() {
        try {
            FileWriter myWriter = new FileWriter("gamestate.txt");
            myWriter.write("");
            for(Entity entity : entities) {
                myWriter.append(String.valueOf(entity.getId())+' ');
                myWriter.append(entity.getType()+' ');
                myWriter.append(String.valueOf(entity.getPlayerId())+' ');
                myWriter.append(String.valueOf(entity.getX())+' ');
                myWriter.append(String.valueOf(entity.getY())+' ');
                myWriter.append(String.valueOf(entity.getReloadTime())+' ');
                myWriter.append(String.valueOf(entity.getHP())+'\n');
            }
            myWriter.append('\n');
            for(Unit unit : units) {
                myWriter.append(String.valueOf(unit.getId())+' ');
                myWriter.append(unit.getType()+' ');
                myWriter.append(String.valueOf(unit.getPlayerId())+' ');
                myWriter.append(String.valueOf(unit.getCurrentTarget().x)+' ');
                myWriter.append(String.valueOf(unit.getCurrentTarget().y)+' ');
            }
            myWriter.append('\n');
            for(Missile missile : missiles) {
                myWriter.append(String.valueOf(missile.getId())+' ');
                myWriter.append(missile.getType()+' ');
                myWriter.append(String.valueOf(missile.getPlayerId())+' ');
                myWriter.append(String.valueOf(missile.getX())+' ');
                myWriter.append(String.valueOf(missile.getY())+' ');
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void getUpdates() {
        try {
            File myObj = new File("requests.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] data = myReader.nextLine().split(" ");
                if(data[0].equals("Spawn")) {
                    if(Config.isUnit(data[1])) {
                        spawnUnit(Float.parseFloat(data[3]), Float.parseFloat(data[4]), data[1], Integer.parseInt(data[2]));
                    }
                    else {
                        spawnTower(Float.parseFloat(data[3]), Float.parseFloat(data[4]), data[1], Integer.parseInt(data[2]));
                    }
                }
                else {
                    sendUnitTo(Integer.parseInt(data[1]), new Vector3(Float.parseFloat(data[2]), Float.parseFloat(data[3]), 0));
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        try {
            FileWriter myWriter = new FileWriter("requests.txt");
            myWriter.write("");
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void reward(int playerId, int amount) {

    }


    public void updateFight() {
        for(Entity entity : entities) {
            entity.update(Config.refreshRate);
        }

        Vector<Entity> objectsToRemove = new Vector<Entity>();
        Vector<Missile> missilesToRemove = new Vector<Missile>();
        for(Entity entity : entities) {
            for(Missile missile : missiles) {
                if(missile.getPlayerId() != entity.playerId && missile.hitObject(entity)){
                    entity.damage(missile.getDamage());
                    missile.targetHit();
                    if(!entity.isAlive()) {
                        reward(missile.getPlayerId(), entity.getReward());
                        objectsToRemove.add(entity);
                    }
                }
                if(!missile.isAlive()) {
                    missilesToRemove.add(missile);
                }
            }
        }
        for(Entity entity : objectsToRemove) {
            entities.remove(entity);
            units.remove(entity);
            entity.remove();
        }
        if(!objectsToRemove.isEmpty()) {
            updateGrid();
            for(Unit unit : units) {
                unit.reconsiderMovement();
            }
        }
        for(Missile missile : missilesToRemove) {
            missiles.remove(missile);
            missile.remove();
        }
        float bestDistance;
        Entity bestTarget;
        for(Entity shooter : entities) {
            bestDistance = 1e9f;
            bestTarget = null;
            for(Entity entity : entities) {
                if(shooter.playerId == entity.playerId)
                    continue;
                float distance = shooter.distance(entity);
                if(distance <= shooter.getRange() && distance < bestDistance) {
                    bestDistance = distance;
                    bestTarget = entity;
                }
            }
            if(bestTarget != null) {
                if(shooter.shoot()) {
                    Missile missile = new Missile(bestTarget, shooter, "missile");
                    missiles.add(missile);
                    stage.addActor(missile);
                }
            }
        }
    }

    public void updateGrid() {
        Vector<Vector3> updates = new Vector<Vector3>();
        for(Entity entity : entities) {
            updates.add(entity.gridUpdate());
        }
        map.updateGrid(updates);
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for(Entity entity : entities) {
            entity.update(Gdx.graphics.getDeltaTime());
        }
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void dispose () {
        stage.dispose();
    }

    public boolean spawnUnit(float x, float y, String type, int playerId) {
        if(map.isPositionBlocked(x, y))
            return false;
        Unit unit = new Unit(type, playerId, map);
        unit.setPosition(x, y, Align.center);
        entities.add(unit);
        units.add(unit);
        stage.addActor(unit.getObjectGroup());
        return true;
    }

    public void sendUnitTo(int id, Vector3 pos) {
        for(Unit unit : units) {
            if(unit.getId() == id) {
                unit.allowTargetChanging(true);
                unit.goToPosition(pos);
                break;
            }
        }
    }

    public boolean spawnTower(float x, float y, String type, int playerId) {
        if(!map.isPositionEmpty(x, y))
            return false;
        Tower tower = new Tower(type, playerId);
        tower.setPosition(x, y, Align.center);
        entities.add(tower);
        stage.addActor(tower.getObjectGroup());
        for(Unit unit : units) {
            unit.reconsiderMovement();
        }
        return true;
    }
}

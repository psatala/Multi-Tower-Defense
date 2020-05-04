package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
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
    private Vector<Unit> units;
    private Vector<Tower> towers;
    private Vector<Missile> missiles;
    private MapActor map;
    protected Stage stage;


    public SuperManager() {
        units = new Vector<>();
        towers = new Vector<>();
        missiles = new Vector<>();
    }

    @Override
    public void create () {
        stage = new Stage(new ScreenViewport());
        map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()-InfoActor.topBarHeight, "map0");
        stage.addActor(map.getMapGroup());

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
            for(Unit unit : units) {
                myWriter.append(unit.toString());
            }
            for(Tower tower : towers) {
                myWriter.append(tower.toString());
            }
            for(Missile missile : missiles) {
                myWriter.append(missile.toString());
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void getUpdates() {
        Vector<String> requests = new Vector<>();
        try {
            File myObj = new File("requests.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                requests.add(myReader.nextLine());
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

        for(String request : requests) {
            String[] data = request.split(" ");
            if(request.charAt(0) == 'S') {
                if(request.charAt(1) == 'U') {
                    spawnUnit(Float.parseFloat(data[3]), Float.parseFloat(data[4]), data[1], Integer.parseInt(data[2]));
                }
                else if(request.charAt(1) == 'T') {
                    spawnTower(Float.parseFloat(data[3]), Float.parseFloat(data[4]), data[1], Integer.parseInt(data[2]));
                }
            }
            else if(request.charAt(0) == 'M') {
                sendUnitTo(Integer.parseInt(data[1]), new Vector3(Float.parseFloat(data[2]), Float.parseFloat(data[3]), 0));
            }
        }
    }

    private void reward(int playerId, int amount) {

    }


    public void updateFight() {
        for(Unit unit : units) {
            unit.update(Config.refreshRate);
        }
        for(Tower tower : towers) {
            tower.update(Config.refreshRate);
        }

        Vector<Unit> unitsToRemove = new Vector<>();
        Vector<Tower> towersToRemove = new Vector<>();
        Vector<Missile> missilesToRemove = new Vector<Missile>();
        for(Unit unit : units) {
            for(Missile missile : missiles) {
                if(missile.getPlayerId() != unit.playerId && missile.hitObject(unit)){
                    unit.damage(missile.getDamage());
                    missile.targetHit();
                    if(!unit.isAlive()) {
                        reward(missile.getPlayerId(), unit.getReward());
                        unitsToRemove.add(unit);
                    }
                }
                if(!missile.isAlive()) {
                    missilesToRemove.add(missile);
                }
            }
        }
        for(Tower tower : towers) {
            for(Missile missile : missiles) {
                if(missile.getPlayerId() != tower.playerId && missile.hitObject(tower)){
                    tower.damage(missile.getDamage());
                    missile.targetHit();
                    if(!tower.isAlive()) {
                        reward(missile.getPlayerId(), tower.getReward());
                        towersToRemove.add(tower);
                    }
                }
                if(!missile.isAlive()) {
                    missilesToRemove.add(missile);
                }
            }
        }

        for(Unit unit : unitsToRemove) {
            units.remove(unit);
            unit.remove();
        }
        for(Tower tower : towersToRemove) {
             towers.remove(tower);
             tower.remove();
        }
        if(!towersToRemove.isEmpty()) {
            updateGrid();
            for(Unit unit : units) {
                unit.reconsiderMovement();
            }
        }
        for(Missile missile : missilesToRemove) {
            missiles.remove(missile);
            missile.remove();
        }

        for(Unit unit : units) {
            findTargetAndShoot(unit);
        }
        for(Tower tower : towers) {
            findTargetAndShoot(tower);
        }
    }

    private void findTargetAndShoot(Entity shooter) {
        float bestDistance = 1e9f;
        Entity bestTarget = null;
        for(Tower tower : towers) {
            if(shooter.playerId == tower.playerId)
                continue;
            float distance = shooter.distance(tower);
            if(distance <= shooter.getRange() && distance < bestDistance) {
                bestDistance = distance;
                bestTarget = tower;
            }
        }
        for(Unit unit : units) {
            if(shooter.playerId == unit.playerId)
                continue;
            float distance = shooter.distance(unit);
            if(distance <= shooter.getRange() && distance < bestDistance) {
                bestDistance = distance;
                bestTarget = unit;
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

    public void updateGrid() {
        Vector<Vector3> updates = new Vector<Vector3>();
        for(Unit unit : units) {
            updates.add(unit.gridUpdate());
        }
        for(Tower tower : towers) {
            updates.add(tower.gridUpdate());
        }
        map.updateGrid(updates);
    }

    @Override
    public void render () {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for(Unit unit : units) {
            unit.update(Gdx.graphics.getDeltaTime());
        }
        for(Tower tower : towers) {
            tower.update(Gdx.graphics.getDeltaTime());
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
        towers.add(tower);
        stage.addActor(tower.getObjectGroup());
        for(Unit unit : units) {
            unit.reconsiderMovement();
        }
        return true;
    }
}

package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.main.Networking.GameRoom;
import com.main.Networking.GameServer;
import com.main.Networking.UpdatesListener;
import com.main.Networking.requests.GameRequest;
import com.main.Networking.responses.GameResponse;
import com.main.Networking.responses.RewardResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class SuperManager{
    private Vector<Unit> units;
    private Vector<Tower> towers;
    private Vector<Missile> missiles;
    private MapActor map;

    private GameResponse gameResponse;
    private RewardResponse rewardResponse;
    public int roomID;
    public GameServer observer;

    public SuperManager() {
        //networking stuff
        gameResponse = new GameResponse();
        rewardResponse = new RewardResponse();


        //other stuff
        units = new Vector<>();
        towers = new Vector<>();
        missiles = new Vector<>();
        map = new MapActor(1080, 720-InfoActor.topBarHeight, "map0", false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateFight();
                updateGrid();
                sendRewards();
                sendUpdates();
            }
        }, 0, (long) (1000/Config.refreshRate));
    }

    public void addObserver(GameServer observer) {
        this.observer = observer;
    }

    private void sendUpdates() {
        if(observer != null) {
            for (Unit unit : units) {
                gameResponse.appendMessage(unit.toString());
            }
            for (Tower tower : towers) {
                gameResponse.appendMessage(tower.toString());
            }
            for (Missile missile : missiles) {
                gameResponse.appendMessage(missile.toString());
            }
            observer.updatesListener.updatesPending(gameResponse, roomID);
            gameResponse.clearMessage();
        }
    }

    private void sendRewards() {
        if(observer != null) {
            observer.updatesListener.updatesPending(rewardResponse, roomID);
            rewardResponse.clearMessage();
        }
    }

    public void getUpdates(GameRequest gameRequest) {
        Vector<String> requests = gameRequest.getMessage();
        /*try {
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
        }*/

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
        String rewardMsg = amount +" \n";
        rewardResponse.appendMessage(rewardMsg);
    }


    public void updateFight() {
        for(Unit unit : units) {
            unit.update(1/Config.refreshRate);
            unit.act(1/Config.refreshRate);
        }
        for(Tower tower : towers) {
            tower.update(1/Config.refreshRate);
            tower.act(1/Config.refreshRate);
        }
        for(Missile missile : missiles) {
            missile.act(1/Config.refreshRate);
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
        for(Missile missile : missiles) {
            if(!missile.isAlive())
                missilesToRemove.add(missile);
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
                Missile missile = new Missile(bestTarget, shooter, "missile", false);
                missiles.add(missile);
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


    public boolean spawnUnit(float x, float y, String type, int playerId) {
        if(map.isPositionBlocked(x, y))
            return false;
        Unit unit = new Unit(type, playerId, map, false);
        unit.setPosition(x, y, Align.center);
        units.add(unit);
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
        Tower tower = new Tower(type, playerId, false);
        tower.setPosition(x, y, Align.center);
        towers.add(tower);
        for(Unit unit : units) {
            unit.reconsiderMovement();
        }
        return true;
    }
}

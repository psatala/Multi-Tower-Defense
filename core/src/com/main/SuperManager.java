package com.main;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.main.Networking.GameServer;
import com.main.Networking.requests.GameRequest;
import com.main.Networking.responses.GameResponse;
import com.main.Networking.responses.RewardResponse;

import java.util.*;

/**
 * This class simulates the game on the server. It contains the only valid state of the game
 * and creates game state updates for the clients. It also receives requests from the clients
 * and updates the game accordingly. All of the actors used in this class are created as undrawable objects.
 * @author Piotr Libera
 */
public class SuperManager{
    private Vector<Unit> units;
    private Vector<Tower> towers;
    private Vector<Missile> missiles;
    private MapActor map;

    private final GameResponse gameResponse;
    private final RewardResponse rewardResponse;
    public int roomID;
    public GameServer observer;

    /**
     * Public constructor of SuperManager<br>
     * Initializes the Timer that fires updates (listed in See Also section) with a frequency specified in Config.
     * @see SuperManager#updateFight()
     * @see SuperManager#updateGrid()
     * @see SuperManager#sendRewards()
     * @see SuperManager#sendUpdates()
     */
    public SuperManager() {
        //networking stuff
        gameResponse = new GameResponse();
        rewardResponse = new RewardResponse();


        //other stuff
        units = new Vector<>();
        towers = new Vector<>();
        missiles = new Vector<>();
        map = new MapActor(1080, 720-InfoActor.topBarHeight, "map1", false);
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

    /**
     * Adds an observer
     * @param observer Observer to be added
     */
    public void addObserver(GameServer observer) {
        this.observer = observer;
    }

    /**
     * Sends the updates containing the entire current game state (state of all Entities and Missiles)
     */
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

    /**
     * Sends rewards to players whose Entities killed other Entities
     */
    private void sendRewards() {
        if(observer != null) {
            observer.updatesListener.updatesPending(rewardResponse, roomID);
            rewardResponse.clearMessage();
        }
    }

    /**
     * Gets updates from players' clients (Spawn and Move Requests)
     * @param gameRequest Request containing all the updates
     */
    public void getUpdates(GameRequest gameRequest) {
        Vector<String> requests = gameRequest.getMessage();

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

    /**
     * Adds a message to reward a player
     * @param playerId ID of the player to reward
     * @param amount Number of coins to reward
     */
    private void reward(int playerId, int amount) {
        String rewardMsg = amount +" \n";
        rewardResponse.appendMessage(rewardMsg);
    }


    /**
     * Updates the game - updates entities and missiles, checks if any missiles hit any entities, or if any entity can take a shot.
     * If a hit was recorded, damage is dealt, and if it kills the hit entity, this entity is removed, and the owner
     * of the hitting missile is rewarded. If any of the towers was destroyed, every unit reconsiders its  movement.
     * @see Unit#update(float)
     * @see Tower#update(float)
     * @see Entity#damage(float)
     * @see Entity#shoot()
     * @see SuperManager#reward(int, int)
     * @see Unit#reconsiderMovement()
     */
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

    /**
     * This method finds a possible target for the given entity. The target candidate is the closest
     * enemy entity to the given entity. If the candidate is within range, and the given entity can take
     * a shot at the given moment, it fires towards the target.
     * @param shooter Shooting entity
     */
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

    /**
     * Collects grid updates from units and towers and updates the grid
     * @see Unit#gridUpdate()
     * @see Tower#gridUpdate()
     * @see MapActor#updateGrid(Vector)
     */
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


    /**
     * Spawns the unit if it's possible (if the given position is not inside a blocked grid cell)
     * @param x X coordinate of the center of the spawned Unit
     * @param y Y coordinate of the center of the spawned Unit
     * @param type Type of the spawned unit (as defined in the config file)
     * @param playerId ID of the player owning this unit
     * @return <code>true</code> if the unit was successfully spawned; <code>false</code> otherwise
     */
    public boolean spawnUnit(float x, float y, String type, int playerId) {
        if(map.isPositionBlocked(x, y))
            return false;
        Unit unit = new Unit(type, playerId, map, false);
        unit.setPosition(x, y, Align.center);
        units.add(unit);
        return true;
    }

    /**
     * Sets the target of a unit with the given id to the given position (regardless if the unit can change
     * its target of not - this mechanism is used only in players' clients and is not needed on the server)
     * @param id ID of the unit to send
     * @param pos Target position
     */
    public void sendUnitTo(int id, Vector3 pos) {
        for(Unit unit : units) {
            if(unit.getId() == id) {
                unit.allowTargetChanging(true);
                unit.goToPosition(pos);
                break;
            }
        }
    }

    /**
     * Spawns the tower if it's possible (if the given position is inside an empty grid cell)
     * @param x X coordinate of the center of the spawned Unit
     * @param y Y coordinate of the center of the spawned Unit
     * @param type Type of the spawned tower (as defined in the config file)
     * @param playerId ID of the player owning this tower
     * @return <code>true</code> if the tower was successfully spawned; <code>false</code> otherwise
     */
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

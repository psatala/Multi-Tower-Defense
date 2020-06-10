package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.main.Networking.GameClient;
import com.main.Networking.requests.GameRequest;
import com.main.Networking.responses.GameResponse;
import com.main.Networking.responses.RewardResponse;

import java.util.List;
import java.util.Vector;

/**
 * This class manages the game on the player's side - it is responsible for receiving commands from the player
 * and creating proper requests for the server. It also simulates the game - although the only valid state
 * of the game is that on the main server. It is therefore updating its state of the game according to
 * updates received from the server. There are two stages in this class - activeStage contains Actors that
 * contain click or hover listeners, passiveStage contains the rest of the Actors<p>
 * The gameManager has only the power to control actions of its player - it controls the coins of this player
 * and checks if their actions are possible based on the local simulation.
 * @author Piotr Libera
 */
public class GameManager extends ApplicationAdapter {
	private int myPlayerId;
	private List<Unit> units;
	private List<Tower> towers;
	private List<Missile> missiles;
	private InfoActor info;
	private MapActor map;
	protected Stage activeStage;
	protected Stage passiveStage;
	private ShapeRenderer renderer;

	public MenuManager menuManager;
	public GameClient observer;
	private final GameRequest gameRequest;
	private Vector<String> objectsToAdd;
	public boolean isRunning = false;
	public boolean needToAddOtherActors = false; //other thread wants to add info and map actor


	/**
	 * Public constructor of GameManager
	 * @param playerId ID of the player
	 */
	public GameManager(int playerId) {
		gameRequest = new GameRequest();
		myPlayerId = playerId;
		units = new Vector<>();
		towers = new Vector<>();
		missiles = new Vector<>();
	}



	/**
	 * Adds an observer
	 * @param observer Given observer
	 * @see GameClient
	 */
	public void addObserver(GameClient observer) {
		this.observer = observer;
	}



	/**
	 * Creates all of libgdx's objects, including the stage that contains every Actor of the client (Interface, Map, Entities on the Map)<br>
	 * Initializes the Timer that fires updates (listed in See Also section) with a frequency specified in Config.
	 * @see GameManager#updateGrid()
	 * @see GameManager#sendUpdates()
	 * @see GameManager#addNewObjectsFromAnotherThread()
	 */
	@Override
	public void create () {
		activeStage = new Stage(new ScreenViewport());
		passiveStage = new Stage(new ScreenViewport());
		menuManager = new MenuManager(observer, activeStage);
		Gdx.input.setInputProcessor(activeStage);
		renderer = new ShapeRenderer();
		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   if(isRunning) {
								   updateGrid();
								   sendUpdates();
								   addNewObjectsFromAnotherThread();
							   }
						   	   else if(needToAddOtherActors) {
						   	   	   addOtherActors();
						   	   	   isRunning = true;
						   	   	   needToAddOtherActors = false;
							   }
						   }
					   }
				,0,1/Config.refreshRate);
	}



	/**
	 * Method called when the game is started (the waiting room phase is completed).
	 * It adds info actor and map actor, and then spawns the main tower.
	 */
	public void addOtherActors() {
		info = new InfoActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this, myPlayerId);
		activeStage.addActor(info.getInfoGroup());
		map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - InfoActor.topBarHeight, this, myPlayerId, "map1", true);
		activeStage.addActor(map.getMapGroup());

		int mainTowerX;
		int mainTowerY;
		switch(myPlayerId) {
			case 0:
				mainTowerX = Config.mainTowerToMapBorderX;
				mainTowerY = Gdx.graphics.getHeight() - (int)InfoActor.topBarHeight - Config.mainTowerToMapBorderY;
				break;
			case 1:
				mainTowerX = Gdx.graphics.getWidth()-Config.mainTowerToMapBorderX;
				mainTowerY = Gdx.graphics.getHeight() - (int)InfoActor.topBarHeight - Config.mainTowerToMapBorderY;
				break;
			case 2:
				mainTowerX = Gdx.graphics.getWidth()-Config.mainTowerToMapBorderX;
				mainTowerY = Config.mainTowerToMapBorderY;
				break;
			default:
				mainTowerX = Config.mainTowerToMapBorderX;
				mainTowerY = Config.mainTowerToMapBorderY;
				break;
		}
		spawnTower(mainTowerX, mainTowerY, "mainTower");
	}



	/**
	 * Method called when player leaves the game. It removes info and map actors.
	 * Moreover, all entities are removed and their corresponding vectors are cleared.
	 */
	public void removeOtherActors() {
		info.getInfoGroup().remove();
		map.getMapGroup().remove();
		for(Unit unit: units)
			unit.remove();
		for(Tower tower: towers)
			tower.remove();
		for(Missile missile: missiles)
			missile.remove();
		units = new Vector<>();
		towers = new Vector<>();
		missiles = new Vector<>();
	}



	/**
	 * Receives rewards from the server and adds them to the player's account
	 * @param rewardResponse Response from the server
	 * @see GameManager#addCoins(int)
	 */
	public void getRewards(RewardResponse rewardResponse) {
		Vector<String> rewards = rewardResponse.getMessage();

		for(String reward : rewards) {
			String r = reward.split(" ")[0];
			addCoins(Integer.parseInt(r));
		}
	}


	/**
	 * Sends updates to the server
	 */
	public void sendUpdates() {
		gameRequest.setRoomID(observer.roomID);
		observer.updatesListener.updatesPending(gameRequest, -1);
		gameRequest.clearMessage();
	}

	/**
	 * Updates the state of the game based on updates from the server.<br>
	 * It deletes killed objects, updates Units, Towers and Missiles, and adds new Entities and Missiles
	 * @param gameResponse Response from the server
	 * @see GameManager#deleteKilledObjects(Vector)
	 */
	public void getUpdates(GameResponse gameResponse) {
		Vector<String> objects = gameResponse.getMessage();

		deleteKilledObjects(objects);

		int id;
		boolean found;
		Vector<String> newObjects = new Vector<>();
		for(String object : objects) {
			id = Integer.parseInt(object.split(" ")[1]);
			found = false;
			if(object.charAt(0) == 'U') {
				for(Unit unit : units) {
					if(id == unit.getId()) {
						unit.setState(object);
						found = true;
						break;
					}
				}
			}
			else if(object.charAt(0) == 'T') {
				for(Tower tower : towers) {
					if(id == tower.getId()) {
						tower.setState(object);
						found = true;
						break;
					}
				}
			}
			else if(object.charAt(0) == 'M') {
				for(Missile missile : missiles) {
					if(id == missile.getId()) {
						missile.setState(object);
						found = true;
						break;
					}
				}
			}
			if(!found) {
				newObjects.add(object);
			}
		}
		objectsToAdd = newObjects; //store objects to add so that the main thread can actually add them
	}


	/**
	 * Adds new objects from another thread
	 */
	private void addNewObjectsFromAnotherThread() {
		if(objectsToAdd != null) {
			addNewObjects(objectsToAdd);
			objectsToAdd.clear();
		}
	}


	/**
	 * Deletes objects that were killed (or fulfilled their purpose in case of Missiles)<br>
	 * After removal, if any towers were destroyed, it also updates the grid and the movement of every Unit.
	 * @param objects Vector containing all of the currently alive objects
	 * @see GameManager#updateGrid()
	 * @see Unit#reconsiderMovement()
	 */
	public void deleteKilledObjects(Vector<String> objects) {
		Vector<Unit> unitsToRemove = new Vector<>();
		Vector<Tower> towersToRemove = new Vector<>();
		Vector<Missile> missilesToRemove = new Vector<>();
		for(Unit unit : units) {
			if(!objectExists(objects, unit.getId(), 'U'))
				unitsToRemove.add(unit);
		}
		for(Tower tower : towers) {
			if(!objectExists(objects, tower.getId(), 'T'))
				towersToRemove.add(tower);
		}
		for(Missile missile : missiles) {
			if(!objectExists(objects, missile.getId(), 'M'))
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
		for(Missile missile : missilesToRemove) {
			missiles.remove(missile);
			missile.remove();
		}
		if(towersToRemove.size() > 0) {
			updateGrid();
			for(Unit unit : units) {
				unit.reconsiderMovement();
			}
		}
	}

	/**
	 * Checks if object is in the given lists of objects
	 * @param objects Vector of the objects
	 * @param id id of the object in question
	 * @param objectType type of the object in question
	 * @return <code>true</code> if the object was found; <code>false</code> otherwise
	 */
	public boolean objectExists(Vector<String> objects, int id, char objectType) {
		for(String object : objects) {
			if(object.charAt(0) == objectType && id == Integer.parseInt(object.split(" ")[1])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates and adds new objects, as they were created on the server and now received
	 * @param objects List of new objects to add
	 */
	public void addNewObjects(Vector<String> objects){
		for(String object : objects) {
			if(object.charAt(0) == 'U') {
				Unit unit = new Unit(object, map, true);
				units.add(unit);
				passiveStage.addActor(unit.getObjectGroup());
			}
			else if(object.charAt(0) == 'T') {
				Tower tower = new Tower(object, true);
				towers.add(tower);
				passiveStage.addActor(tower.getObjectGroup());
			}
			else if(object.charAt(0) == 'M') {
				Missile missile = new Missile(object, true);
				missiles.add(missile);
				passiveStage.addActor(missile);
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
	    for(Tower tower : towers) {
	        updates.add(tower.gridUpdate());
        }
	    for(Unit unit : units) {
	    	updates.add(unit.gridUpdate());
		}
	    map.updateGrid(updates);
    }

	/**
	 * Updates towers and units and draws every actor in the game.
	 * @see Unit#update(float)
	 * @see Tower#update(float)
	 */
	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for(Tower tower : towers) {
			tower.update(Gdx.graphics.getDeltaTime());
		}
		for(Unit unit : units) {
			unit.update(Gdx.graphics.getDeltaTime());
		}
		activeStage.act(Gdx.graphics.getDeltaTime());
		activeStage.draw();
		passiveStage.act(Gdx.graphics.getDeltaTime());
		passiveStage.draw();
	}

	/**
	 * Method for resizing the window (currently turned off)
	 * @param width New Width of the window
	 * @param height New Height of the window
	 */
	public void resize (int width, int height) {
		activeStage.getViewport().update(width, height, true);
		passiveStage.getViewport().update(width, height, true);
	}

	/**
	 * Overrides libgdx's dispose() method and calls dispose() on renderer and both stages.
	 */
	@Override
	public void dispose () {
		renderer.dispose();
		activeStage.dispose();
		passiveStage.dispose();
	}

	/**
	 * Calls the unit spawn request creator
	 * @param x X coordinate of the center of a new unit
	 * @param y Y coordinate of the center of a new unit
	 * @param type type of the new unit as defined in the config file
	 * @see GameManager#sendSpawnRequest(float, float, char, String)
	 */
	public void spawnUnit(float x, float y, String type) {
		if(!info.spendCoins(Config.objectCost.get(type)))
			return;
		sendSpawnRequest(x, y, 'U', type);
	}

	/**
	 * Calls the move request creator
	 * @param pos Position to send units to
	 * @see GameManager#sendMoveRequest(int, Vector3)
	 * @see SuperManager#sendUnitTo(int, Vector3)
	 */
	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			if(unit.isTargetChangeable()) {
				sendMoveRequest(unit.getId(), pos);
			}
		}
	}

	/**
	 * Calls the tower spawn request creator
	 * @param x X coordinate of the center of a new tower
	 * @param y Y coordinate of the center of a new tower
	 * @param type type of the new tower as defined in the config file
	 * @see GameManager#sendSpawnRequest(float, float, char, String)
	 */
	public void spawnTower(float x, float y, String type) {
		if(!info.spendCoins(Config.objectCost.get(type)))
			return;
		sendSpawnRequest(x, y, 'T', type);
	}

	/**
	 * Selects player's units based on their selection and allows them to change their target, while forbiding every
	 * other unit to do so.
	 * @param rect Player's selection
	 * @see Unit#allowTargetChanging(boolean)
	 */
	public void selectUnits(Rectangle rect) {
		float unitX, unitY;
		for(Unit unit : units) {
			if(unit.getPlayerId() != myPlayerId)
				continue;
			unitX = unit.getX(Align.center);
			unitY = unit.getY(Align.center);
			if(unitX >= rect.x && unitX <= rect.x+rect.width && unitY >= rect.y && unitY <= rect.y+rect.height) {
				unit.allowTargetChanging(true);
			}
			else {
				unit.allowTargetChanging(false);
			}
		}
	}

	/**
	 * Sets the Mode of the Map
	 * @param mode Mode to change to
	 * @see MapActor#setMode(MapActor.Mode)
	 */
	public void setMode(MapActor.Mode mode) {
		map.setMode(mode);
	}

	/**
	 * Setter method
	 * @param newPlayerId Player's ID
	 */
	public void setPlayerId(int newPlayerId) { myPlayerId = newPlayerId; }

	/**
	 * Getter method
	 * @return Player's ID
	 */
	public int getPlayerId() {
		return myPlayerId;
	}

	/**
	 * Adds the given number of coins to the player's account
	 * @param number Number of coins to add
	 * @see InfoActor#addCoins(int)
	 */
	public void addCoins(int number) {
		info.addCoins(number);
	}

	/**
	 * Creates the request and calls sendRequest()
	 * @param x X coordinate of the center of the new entity
	 * @param y Y coordinate of the center of the new entity
	 * @param entityType 'U' for Unit, 'T' for Tower
	 * @param type type of the new Entity as defined in the config file
	 * @see GameManager#sendRequest(String)
	 */
	public void sendSpawnRequest(float x, float y, char entityType, String type) {
		String request = "S" + entityType + " ";
		request += type + " ";
		request += myPlayerId + " ";
		request += x + " ";
		request += y + " ";
		request += "\n";
		sendRequest(request);
	}

	/**
	 * Creates the request and calls sendRequest()
	 * @param id ID of the unit to move
	 * @param pos Target position of the unit
	 * @see GameManager#sendRequest(String)
	 */
	public void sendMoveRequest(int id, Vector3 pos) {
		String request = "M ";
		request += id + " ";
		request += pos.x + " ";
		request += pos.y + " ";
		request += "\n";
		sendRequest(request);
	}

	/**
	 * Passes the request to GameRequest object
	 * @param request Given request
	 * @see GameRequest#appendMessage(String)
	 */
	public void sendRequest(String request) {
		gameRequest.appendMessage(request);
	}
}

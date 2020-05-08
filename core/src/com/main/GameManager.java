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
import com.main.Networking.UpdatesListener;
import com.main.Networking.requests.GameRequest;
import com.main.Networking.responses.GameResponse;
import com.main.Networking.responses.RewardResponse;

import java.io.IOException;
import java.util.List;
import java.util.Vector;


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

	private GameClient gameClient;
	private GameRequest gameRequest;
	public UpdatesListener updatesListener;
	private Vector<String> objectsToAdd;

	public GameManager(int playerId) {
		//networking stuff
		updatesListener = new UpdatesListener() {
			@Override
			public void updatesReceived(Object object) {
				if(object instanceof RewardResponse) {
					RewardResponse rewardResponse = (RewardResponse)object;
					getRewards(rewardResponse);
				}
				else if (object instanceof GameResponse) {
					GameResponse gameResponse = (GameResponse)object;
					if(gameResponse.message.size() > 0) {
						int debugHere = 0;
					}
					getUpdates(gameResponse);
				}
			}
		};

		try {
			gameClient = new GameClient(54545, 54545, 54546, 54546, 500);
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		gameClient.addObserver(this);
		gameRequest = new GameRequest();




		//other stuff
		myPlayerId = playerId;
		units = new Vector<>();
		towers = new Vector<>();
		missiles = new Vector<>();
	}

	@Override
	public void create () {
		activeStage = new Stage(new ScreenViewport());
		passiveStage = new Stage(new ScreenViewport());
		info = new InfoActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this, myPlayerId);
		activeStage.addActor(info.getInfoGroup());
		map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - InfoActor.topBarHeight, this, myPlayerId, "map0", true);
		activeStage.addActor(map.getMapGroup());
		Gdx.input.setInputProcessor(activeStage);
		renderer = new ShapeRenderer();

		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   updateGrid();
						   	   sendUpdates();
						   	   addNewObjectsFromAnotherThread();
						   }
					   }
				,0,1/Config.refreshRate);
	}

	public void getRewards(RewardResponse rewardResponse) {
		Vector<String> rewards = rewardResponse.getMessage();
		/*try {
			File myObj = new File("rewards"+myPlayerId+".txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				rewards.add(myReader.nextLine());
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
		try {
			FileWriter myWriter = new FileWriter("rewards"+myPlayerId+".txt");
			myWriter.write("");
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}*/

		for(String reward : rewards) {
			String r = reward.split(" ")[0];
			addCoins(Integer.parseInt(r));
		}
	}


	public void sendUpdates() {
		gameRequest.setRoomID(gameClient.roomID);
		gameClient.send(gameRequest);
		gameRequest.clearMessage();
	}

	public void getUpdates(GameResponse gameResponse) {
		Vector<String> objects = gameResponse.getMessage();
		/*try {
			File myObj = new File("gamestate.txt");
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				objects.add(myReader.nextLine());
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}*/

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



	private void addNewObjectsFromAnotherThread() {
		addNewObjects(objectsToAdd);
		objectsToAdd.clear();
	}



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
		//if(towersToRemove.size() > 0) {
		//	for(Unit unit : units) {
		//		unit.reconsiderMovement();
		//	}
		//}
	}

	public boolean objectExists(Vector<String> objects, int id, char objectType) {
		for(String object : objects) {
			if(object.charAt(0) == objectType && id == Integer.parseInt(object.split(" ")[1])) {
				return true;
			}
		}
		return false;
	}

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

	public void resize (int width, int height) {
		activeStage.getViewport().update(width, height, true);
		passiveStage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose () {
		renderer.dispose();
		activeStage.dispose();
		passiveStage.dispose();
	}

	public void spawnUnit(float x, float y, String type) {
		if(!info.spendCoins(Config.objectCost.get(type)))
			return;
		sendSpawnRequest(x, y, 'U', type);
	}

	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			if(unit.isTargetChangeable()) {
				sendMoveRequest(unit.getId(), pos);
			}
		}
	}

	public void spawnTower(float x, float y, String type) {
		if(!info.spendCoins(Config.objectCost.get(type)))
			return;
		sendSpawnRequest(x, y, 'T', type);
	}

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

	public void setMode(MapActor.Mode mode) {
		map.setMode(mode);
	}

	public int getPlayerId() {
		return myPlayerId;
	}

	public void addCoins(int amount) {
		info.addCoins(amount);
	}


	public void sendSpawnRequest(float x, float y, char entityType, String type) {
		String request = "S" + entityType + " ";
		request += type + " ";
		request += myPlayerId + " ";
		request += x + " ";
		request += y + " ";
		request += "\n";
		sendRequest(request);
	}

	public void sendMoveRequest(int id, Vector3 pos) {
		String request = "M ";
		request += id + " ";
		request += pos.x + " ";
		request += pos.y + " ";
		request += "\n";
		sendRequest(request);
	}

	public void sendRequest(String request) {
		gameRequest.appendMessage(request);
	}
}

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;


public class GameManager extends ApplicationAdapter {
	private int myPlayerId;
	private List<Entity> entities;
	private List<Unit> units;
	private List<Missile> missiles;
	private InfoActor info;
	private MapActor map;
	protected Stage activeStage;
	protected Stage passiveStage;
	private ShapeRenderer renderer;

	public GameManager(int playerId) {
		myPlayerId = playerId;
		units = new Vector<>();
		entities = new Vector<>();
		missiles = new Vector<>();
	}

	@Override
	public void create () {
		activeStage = new Stage(new ScreenViewport());
		passiveStage = new Stage(new ScreenViewport());
		info = new InfoActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this, myPlayerId);
		activeStage.addActor(info.getInfoGroup());
		map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - InfoActor.topBarHeight, this, myPlayerId, "map0");
		activeStage.addActor(map.getMapGroup());
		Gdx.input.setInputProcessor(activeStage);
		renderer = new ShapeRenderer();

		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   updateGrid();
						   	   getUpdates();
						   }
					   }
				,0,1/Config.refreshRate);
	}

	public void getUpdates() {
		try {
			File myObj = new File("gamestate.txt");
			Scanner myReader = new Scanner(myObj);
			int count = 0;
			Vector<Entity> newEntities = new Vector<>();
			Vector<Unit> newUnits = new Vector<>();
			Vector<Missile> newMissiles = new Vector<>();
			while (myReader.hasNextLine()) {
				String[] data = myReader.nextLine().split(" ");
				if(data.length <= 2) {
					count++;
					continue;
				}
				if(count == 0) {
					Entity e = new Entity(data[1], Integer.parseInt(data[2]));
					e.setId(Integer.parseInt(data[0]));
					e.setPosition(Float.parseFloat(data[3]), Float.parseFloat(data[4]));
					e.setReloadTime(Float.parseFloat(data[5]));
					e.setHP(Float.parseFloat(data[6]));
					newEntities.add(e);
				}
				else if(count == 1) {
					Unit unit = new Unit("firstUnit", 0, map);
					unit.setId(Integer.parseInt(data[0]));
					unit.allowTargetChanging(true);
					unit.goToPosition(new Vector3(Float.parseFloat(data[1]), Float.parseFloat(data[2]), 0));
					newUnits.add(unit);
				}
				else if(count == 2) {
					Missile m = new Missile(new Vector3(0, 0, 0), new Vector3(0, 0, 0), 0, data[1], Integer.parseInt(data[2]));
					m.setId(Integer.parseInt(data[0]));
					m.setPosition(Float.parseFloat(data[3]), Float.parseFloat(data[4]), 0);
					newMissiles.add(m);
				}
			}
			myReader.close();
			updateEntities(newEntities);
			updateUnits(newUnits);
			updateMissiles(newMissiles);
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
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
		if(!info.spendCoins(Config.objectCost.get(type)) || map.isPositionBlocked(x, y))
			return;
		sendSpawnRequest(x, y, type);
	}

	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			if(unit.isTargetChangeable()) {
				sendMoveRequest(unit.getId(), pos);
			}
		}
	}

	public void spawnTower(float x, float y, String type) {
		if(!info.spendCoins(Config.objectCost.get(type)) || !map.isPositionEmpty(x, y))
			return;
		sendSpawnRequest(x, y, type);
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

	public void addEntity(Entity e) {
		if(e.entityType == Entity.Type.TOWER) {
			Tower tower = new Tower(e.getType(), e.getPlayerId());
			tower.setPosition(e.getX(), e.getY());
			passiveStage.addActor(tower.getObjectGroup());
			entities.add(tower);
		}
		else {
			Unit unit = new Unit(e.getType(), e.getPlayerId(), map);
			unit.setPosition(e.getX(), e.getY());
			entities.add(unit);
			units.add(unit);
			passiveStage.addActor(unit.getObjectGroup());
		}
	}

	public void deleteEntity(Entity e) {
		entities.remove(e);
		units.remove(e);
		e.remove();
	}

	public void addMissile(Missile m) {
		Missile missile = new Missile(m.getTarget(), m.getPosition(Align.center), m.getDamage(), m.getType(), m.getPlayerId());
		missiles.add(missile);
		passiveStage.addActor(missile);
	}

	public void deleteMissile(Missile m) {
		missiles.remove(m);
		m.remove();
	}

	public void updateEntities(Vector<Entity> newEntitiesVector) {
		boolean found;
		for(Entity entity : newEntitiesVector) {
			found = false;
			for(Entity ent2 : entities) {
				if(entity.getId() == ent2.getId()) {
					ent2.setPosition(entity.getX(), entity.getY());
					ent2.setReloadTime(entity.getReloadTime());
					ent2.setHP(entity.getHP());
					found = true;
					break;
				}
			}
			if(!found) {
				addEntity(entity);
			}
		}
		Vector<Entity> killedEntities = new Vector<>();
		for(Entity ent2 : entities) {
			found = false;
			for(Entity entity : newEntitiesVector) {
				if(ent2.getId() == entity.getId()) {
					found = true;
					break;
				}
			}
			if(!found) {
				killedEntities.add(ent2);
			}
		}
		for(Entity ent : killedEntities)
			deleteEntity(ent);
	}

	public void updateMissiles(Vector<Missile> newMissilesVector) {
		boolean found;
		for(Missile missile : newMissilesVector) {
			found = false;
			for(Missile myM : missiles) {
				if(myM.getId() == missile.getId()) {
					myM.setPosition(missile.getX(), missile.getY());
					found = true;
					break;
				}
			}
			if(!found) {
				addMissile(missile);
			}
		}
		Vector<Missile> killedMissiles = new Vector<>();
		for(Missile myM : missiles) {
			found = false;
			for(Missile missile : newMissilesVector) {
				if(myM.getId() == missile.getId()) {
					found = true;
					break;
				}
			}
			if(!found) {
				killedMissiles.add(myM);
			}
		}
		for(Missile m : killedMissiles)
			deleteMissile(m);
	}

	public void updateUnits(Vector<Unit> newUnitsVector) {
		for(Unit unit : newUnitsVector) {
			for(Unit myUnit : units) {
				if(myUnit.getId() == unit.getId()) {
					myUnit.allowTargetChanging(true);
					myUnit.goToPosition(unit.getCurrentTarget());
					break;
				}
			}
		}
	}

	public void sendSpawnRequest(float x, float y, String type) {
		try {
			FileWriter myWriter = new FileWriter("requests.txt");
			myWriter.append("Spawn ");
			myWriter.append(type+' ');
			myWriter.append(String.valueOf(myPlayerId)+' ');
			myWriter.append(String.valueOf(x)+' ');
			myWriter.append(String.valueOf(y)+' ');
			myWriter.append('\n');
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public void sendMoveRequest(int id, Vector3 pos) {
		try {
			FileWriter myWriter = new FileWriter("requests.txt");
			myWriter.append("Move ");
			myWriter.append(String.valueOf(id)+' ');
			myWriter.append(String.valueOf(pos.x)+' ');
			myWriter.append(String.valueOf(pos.y)+' ');
			myWriter.append('\n');
			myWriter.close();
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}

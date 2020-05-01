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

import java.util.List;
import java.util.Vector;

import static java.lang.Math.random;


public class GameManager extends ApplicationAdapter {
	private List<Entity> entities;
	private List<Unit> units;
	private List<Missile> missiles;
	private InfoActor info;
	private MapActor map;
	protected Stage activeStage;
	protected Stage passiveStage;
	private ShapeRenderer renderer;

	@Override
	public void create () {
		activeStage = new Stage(new ScreenViewport());
		passiveStage = new Stage(new ScreenViewport());
		info = new InfoActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), this, 0);
		activeStage.addActor(info.getInfoGroup());
		map = new MapActor(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - InfoActor.topBarHeight, this, 0, "map0");
		activeStage.addActor(map.getMapGroup());
		Gdx.input.setInputProcessor(activeStage);
		renderer = new ShapeRenderer();
		units = new Vector<Unit>();
		entities = new Vector<Entity>();
		missiles = new Vector<Missile>();
		for(int i = 0; i < 10; ++i)
		    spawnUnit((float)random()*300+50, (float)random()*600, 0);
		for(int i = 0; i < 0; ++i)
			spawnUnit((float)random()*300+700, (float)random()*600, 1);

		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   updateFight();
						   	   updateGrid();
						   }
					   }
				,0,1/30.0f);
	}

	public void updateFight() {
		Vector<Entity> objectsToRemove = new Vector<Entity>();
		Vector<Missile> missilesToRemove = new Vector<Missile>();
	    for(Entity entity : entities) {
	        for(Missile missile : missiles) {
	            if(missile.getPlayerId() != entity.playerId && missile.hitObject(entity)){
	                entity.damage(missile.getDamage());
	                missile.targetHit();
	                if(!entity.isAlive()) {
	                	if(missile.getPlayerId() == 0) {
							info.addCoins(entity.getReward());
						}
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
            		passiveStage.addActor(missile);
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
			entity.update();
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

	public void spawnUnit(float x, float y, int playerId) {
		Unit unit = new Unit("firstUnit", playerId, this);
		if(playerId == 0 && !info.spendCoins(unit.getCost()))
			return;
		unit.setPosition(x, y, Align.center);
		entities.add(unit);
		units.add(unit);
		passiveStage.addActor(unit.getObjectGroup());
	}

	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			unit.goToPosition(pos);
		}
	}

	public void spawnTower(float x, float y, int playerId) {
		Tower tower = new Tower("firstTower", playerId);
		if(playerId == 0 && !info.spendCoins(tower.getCost()))
			return;
		tower.setPosition(x, y, Align.center);
		entities.add(tower);
		passiveStage.addActor(tower.getObjectGroup());
		for(Unit unit : units) {
			unit.reconsiderMovement();
		}
	}

	public void selectUnits(Rectangle rect) {
		float unitX, unitY;
		for(Unit unit : units) {
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

	public Vector<Vector3> findPath(Vector3 start, Vector3 finish) {
		return map.findPath(start, finish);
	}

	public float getMapWidth() {
		return map.getWidth();
	}

	public float getMapHeight() {
		return map.getHeight();
	}
}

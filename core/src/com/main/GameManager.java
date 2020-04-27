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
	private List<Object> objects;
	private List<Unit> units;
	private List<Missile> missiles;
	private PlayerInterface mainInterface;
	protected Stage stage;
	private ShapeRenderer renderer;

	@Override
	public void create () {
		stage = new Stage(new ScreenViewport());
		mainInterface = new PlayerInterface(this, 0);
		stage.addActor(mainInterface);
		Gdx.input.setInputProcessor(stage);
		renderer = new ShapeRenderer();
		units = new Vector<Unit>();
		objects  = new Vector<Object>();
		missiles = new Vector<Missile>();
		for(int i = 0; i < 4; ++i)
		    spawnUnit((float)random()*300+50, (float)random()*100+i*150+50, 0);
		for(int i = 0; i < 4; ++i)
			spawnUnit((float)random()*300+700, (float)random()*100+i*150+50, 1);

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
	    for(Object object : objects) {
	        for(Missile missile : missiles) {
	            if(missile.getPlayerId() != object.playerId && missile.hitObject(object)){
	                object.damage(missile.getDamage());
	                missile.targetHit();
	                if(!object.isAlive() && missile.getPlayerId()==0) {
	                	mainInterface.addCoins(object.getReward());
					}
                }
            }
        }
		float bestDistance;
		Object bestTarget;
		for(Object shooter : objects) {
			bestDistance = 1e9f;
			bestTarget = null;
            for(Object object : objects) {
            	if(shooter.playerId == object.playerId)
            		continue;
            	float distance = shooter.distance(object);
            	if(distance <= shooter.range && distance < bestDistance) {
            		bestDistance = distance;
            		bestTarget = object;
				}
			}
            if(bestTarget != null) {
            	if(shooter.shoot()) {
            		Missile missile = new Missile(bestTarget, shooter);
            		missiles.add(missile);
            		stage.addActor(missile);
				}
			}
		}
	}

	public void updateGrid() {
	    Vector<Vector3> updates = new Vector<Vector3>();
	    for(Object object : objects) {
	        updates.add(object.gridUpdate());
        }
	    mainInterface.updateGrid(updates);
    }

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		for(Object object : objects)
			object.update();
		Vector<Missile> missilesToRemove = new Vector<Missile>();
		Vector<Object> unitsToRemove = new Vector<Object>();
		for (Missile missile : missiles) {
			if (!missile.isAlive()) {
				missilesToRemove.add(missile);
			}
		}
		for (Missile missile : missilesToRemove) {
			missiles.remove(missile);
			missile.remove();
		}
		for (Object object : objects) {
			if (!object.isAlive()) {
				unitsToRemove.add(object);
			}
		}
		for (Object object : unitsToRemove) {
			objects.remove(object);
			units.remove(object);
			object.remove();
		}

		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	public void resize (int width, int height) {
		stage.getViewport().update(width, height, true);
	}
	
	@Override
	public void dispose () {
		renderer.dispose();
		stage.dispose();
	}

	private void spawnUnit(float x, float y, int playerId) {
		Unit unit = new Unit("firstUnit", playerId);
		if(playerId == 0 && !mainInterface.spendCoins(unit.getCost()))
			return;
		unit.setPosition(x, y, Align.center);
		objects.add(unit);
		units.add(unit);
		stage.addActor(unit);
		stage.addActor(unit.healthbar);
	}

	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			unit.goToPosition(pos);
		}
	}

	public void spawnTower(float x, float y, int playerId) {
		Tower tower = new Tower("firstTower", playerId);
		if(playerId == 0 && !mainInterface.spendCoins(tower.getCost()))
			return;
		tower.setPosition(x, y, Align.center);
		objects.add(tower);
		stage.addActor(tower);
		stage.addActor(tower.healthbar);
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
}

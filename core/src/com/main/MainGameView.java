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


public class MainGameView extends ApplicationAdapter {
	private List<Unit> units;
	private List<Missile> missiles;
	private PlayerInterface mainInterface;
	private Stage stage;
	private ShapeRenderer renderer;

	@Override
	public void create () {
		stage = new Stage(new ScreenViewport());
		renderer = new ShapeRenderer();
		units = new Vector<Unit>();
		missiles = new Vector<Missile>();
		for(int i = 0; i < 4; ++i)
		    spawnUnit((float)random()*300+50, (float)random()*100+i*150+50, 0);
		for(int i = 0; i < 4; ++i)
			spawnUnit((float)random()*300+700, (float)random()*100+i*150+50, 1);
		mainInterface = new PlayerInterface(this);
		stage.addActor(mainInterface);
		Gdx.input.setInputProcessor(stage);

		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   updateFight();
						   }
					   }
				,0,1/30.0f);
	}

	public void updateFight() {
	    for(Unit unit : units) {
	        for(Missile missile : missiles) {
	            if(missile.getMissileColor() != unit.color && missile.hitObject(unit)){
	                unit.damage(missile.getDamage());
	                missile.targetHit();
                }
            }
        }
		float bestDistance;
		Object bestTarget;
		for(Unit shooter : units) {
			bestDistance = 1e9f;
			bestTarget = null;
            for(Unit unit : units) {
            	if(shooter.color == unit.color)
            		continue;
            	float distance = shooter.distance(unit);
            	if(distance <= shooter.range && distance < bestDistance) {
            		bestDistance = distance;
            		bestTarget = unit;
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

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


		for (Unit unit : units)
			unit.update();
		Vector<Missile> missilesToRemove = new Vector<Missile>();
		Vector<Unit> unitsToRemove = new Vector<Unit>();
		for (Missile missile : missiles) {
			if (!missile.isAlive()) {
				missilesToRemove.add(missile);
			}
		}
		for (Missile missile : missilesToRemove) {
			missiles.remove(missile);
			missile.remove();
		}
		for (Unit unit : units) {
			if (!unit.isAlive()) {
				unitsToRemove.add(unit);
			}
		}
		for (Unit unit : unitsToRemove) {
			units.remove(unit);
			unit.remove();
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

	private void spawnUnit(float x, float y, int color) {
		Unit unit = new Unit("firstUnit", color);
		unit.setPosition(x, y, Align.center);
		units.add(unit);
		stage.addActor(unit);
		stage.addActor(unit.healthbar);
	}

	public void sendUnitsTo(Vector3 pos) {
		for(Unit unit : units) {
			unit.goToPosition(pos);
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
}

package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer;

import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.StrictMath.max;


public class MainGameView extends ApplicationAdapter implements InputProcessor {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Vector<Unit> units;
	private Vector<Missile> missiles;
	private PlayerInterface mainInterface;
	private Vector3 selection;
	private Rectangle selectRect;
	private boolean drawSelection = false;

	private ShapeRenderer renderer;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		units = new Vector<Unit>();
		missiles = new Vector<Missile>();
		for(int i = 0; i < 4; ++i)
		    spawnUnit((float)random()*300+50, (float)random()*100+i*150+50, 0);
		for(int i = 0; i < 4; ++i)
			spawnUnit((float)random()*300+700, (float)random()*100+i*150+50, 1);
		mainInterface = new PlayerInterface();
		Gdx.input.setInputProcessor(this);
		renderer = new ShapeRenderer();

		Timer.schedule(new Timer.Task(){
						   @Override
						   public void run() {
						   	   updateFight();
						   }
					   }
				,0,1/30.0f);
	}

	public void updateFight() {
		float bestDistance;
		Object bestTarget;
		for(Unit shooter : units) {
			bestDistance = 1e9f;
			bestTarget = null;
            for(Unit unit : units) {
            	if(shooter.color == unit.color)
            		continue;
            	float distance = shooter.distance(unit.getPosition());
            	if(distance <= shooter.range && distance < bestDistance) {
            		bestDistance = distance;
            		bestTarget = unit;
				}
			}
            if(bestTarget != null) {
            	if(shooter.shoot()) {
            		missiles.add(new Missile(bestTarget, shooter));
				}
			}
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		for(Unit unit : units)
			unit.update();

		Vector<Missile> missilesToRemove = new Vector<Missile>();
		Vector<Unit> unitsToRemove = new Vector<Unit>();
		for(Missile missile : missiles) {
			if(missile.update()) {
				missilesToRemove.add(missile);
			}
		}
		for(Missile missile : missilesToRemove) {
			missiles.remove(missile);
		}
		for(Unit unit : units) {
			if(!unit.isAlive()) {
				unitsToRemove.add(unit);
			}
		}
		for(Unit unit : unitsToRemove) {
			units.remove(unit);
		}

		renderer.begin(ShapeRenderer.ShapeType.Filled);
		if(drawSelection){
			renderer.setColor(Color.DARK_GRAY);
			renderer.rect(selectRect.x, selectRect.y, selectRect.width, selectRect.height);
		}
		for(Unit unit : units) {
			unit.draw(renderer);
		}
		renderer.end();
		batch.begin();
		for(Unit unit : units)
			unit.draw(batch);
		for(Missile missile : missiles)
			missile.draw(batch);
		mainInterface.draw(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		for(Unit unit : units)
			unit.dispose();
		mainInterface.dispose();
		renderer.dispose();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		selection = new Vector3(screenX, screenY, 0);
		camera.unproject(selection);
		if(button == Input.Buttons.RIGHT){
			selectRect = new Rectangle(selection.x, selection.y, 0, 0);
			drawSelection = true;
		}
		else if(button == Input.Buttons.LEFT){
			for(Unit unit : units) {
				unit.setTarget(selection);
			}
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(!drawSelection)
			return false;
		Vector3 selection2 = new Vector3(screenX, screenY, 0);
		camera.unproject(selection2);
        float tlx = min(selection.x, selection2.x);
        float tly = max(selection.y, selection2.y);
        float brx = max(selection.x, selection2.x);
        float bry = min(selection.y, selection2.y);
        float unitX, unitY;
		for(Unit unit : units) {
			unitX = unit.getX();
			unitY = unit.getY();
        	if(unitX >= tlx && unitX <= brx && unitY <= tly && unitY >= bry) {
        		unit.allowTargetChanging(true);
			}
        	else {
        		unit.allowTargetChanging(false);
			}
		}
		drawSelection = false;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(!drawSelection)
			return false;
        Vector3 dragSelect = new Vector3(screenX, screenY, 0);
        camera.unproject(dragSelect);
        selectRect.x = min(selection.x, dragSelect.x);
        selectRect.y = min(selection.y, dragSelect.y);
        selectRect.width = abs(selection.x - dragSelect.x);
        selectRect.height = abs(selection.y - dragSelect.y);
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}


	private void spawnUnit(float x, float y, int color) {
		units.add(new Unit(x, y, "staszic", color));
	}
}

package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.Vector;

import static java.lang.Math.min;
import static java.lang.Math.random;
import static java.lang.StrictMath.max;


public class MainGameView extends ApplicationAdapter implements InputProcessor {
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private Vector<Unit> units;
	private PlayerInterface mainInterface;
	private int selectionx;
	private int selectiony;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		units = new Vector();
		for(int i = 0; i < 20; ++i)
		    spawnUnit((float)random()*1000, (float)random()*650);
		mainInterface = new PlayerInterface();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		for(Unit unit : units)
			unit.update();

		batch.begin();
		for(Unit unit : units)
			unit.draw(batch);
		mainInterface.draw(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		for(Unit unit : units)
			unit.dispose();
		mainInterface.dispose();
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		System.out.println("YYY");
		selectionx = screenX;
		selectiony = screenY;
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		System.out.println("XXX");
        float tlx = min(selectionx, screenX);
        float tly = max(selectiony, screenY);
        float brx = max(selectionx, screenX);
        float bry = min(selectiony, screenY);
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
	public boolean touchDragged(int screenX, int screenY, int pointer) {
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


	private void spawnUnit(float x, float y) {
		units.add(new Unit(x, y, camera));
	}
}

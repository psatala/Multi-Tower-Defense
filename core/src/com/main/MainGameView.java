package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Timer.Task;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Timer;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class MainGameView extends ApplicationAdapter {
	private SpriteBatch batch;

	private BitmapFont font;
	private Texture texture;
	private Sprite sprite;

	private Pixmap pixmap;
	private Texture line;
	private Sprite lineSprite;

	private TextureAtlas textureAtlas;
	private int currentFrame = 0;
	private String currentAtlasKey = new String("firstUnit0");
	private Sprite atlasSprite;

	private OrthographicCamera camera;

	private float targetx;
	private float targety;
	private boolean unitMoving = false;
	
	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 1080, 720);

		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		texture = new Texture(Gdx.files.internal("plus.png"));
		sprite = new Sprite(texture);
		pixmap = new Pixmap(1080, 2, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.WHITE);
		pixmap.fill();
		line = new Texture(pixmap);
		pixmap.dispose();
		lineSprite = new Sprite(line);

		textureAtlas = new TextureAtlas(Gdx.files.internal("units/firstUnit/firstUnit.atlas"));
		TextureAtlas.AtlasRegion region = textureAtlas.findRegion("firstUnit0");
		atlasSprite = new Sprite(region);
		atlasSprite.setPosition(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2);
		targetx = Gdx.graphics.getWidth()/2;
		targety = Gdx.graphics.getHeight()/2;
		Timer.schedule(new Task(){
						   @Override
						   public void run() {
							   if(unitMoving)
							   	   currentFrame++;
							   else
							   	   currentFrame = 0;
							   if(currentFrame >= 4)
								   currentFrame = 0;

							   currentAtlasKey = String.format("firstUnit%d", currentFrame);
							   atlasSprite.setRegion(textureAtlas.findRegion(currentAtlasKey));
							   System.out.println(currentFrame);
						   }
					   }
				,0,1/8.0f);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		if(Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			targetx = touchPos.x-32;
			targety = touchPos.y-32;
		}

		System.out.println(String.valueOf(atlasSprite.getX()) + "   " + String.valueOf(atlasSprite.getY()));

		float deltaDist = 150*Gdx.graphics.getDeltaTime();
		float distX = abs(targetx - atlasSprite.getX());
		float distY = abs(targety - atlasSprite.getY());
		if(distX*distX + distY*distY > deltaDist*deltaDist) {
			unitMoving = true;
			float dx, dy;
			if(distY == 0){
				dy = 0;
				dx = deltaDist;
			}
			else {
				dy = (float)(deltaDist/sqrt(1+pow(distX/distY, 2)));
				dx = dy*distX/distY;
			}
			if(atlasSprite.getX() < targetx)
			    atlasSprite.setX(atlasSprite.getX() + dx);
			else
				atlasSprite.setX(atlasSprite.getX() - dx);
			if(atlasSprite.getY() < targety)
			    atlasSprite.setY(atlasSprite.getY() + dy);
			else
				atlasSprite.setY(atlasSprite.getY() - dy);
		}
		else {
			unitMoving = false;
			atlasSprite.setPosition(targetx, targety);
		}

		batch.begin();
		font.draw(batch, "Player 1", 530, 710);
		font.draw(batch, "Coins: You're broke.", 940, 710);
		sprite.setPosition(targetx, targety);
		sprite.draw(batch);
		lineSprite.setPosition(0, 686);
		lineSprite.draw(batch);
		atlasSprite.draw(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		texture.dispose();
		line.dispose();
		textureAtlas.dispose();
	}
}

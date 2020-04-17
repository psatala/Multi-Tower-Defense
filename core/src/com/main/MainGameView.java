package com.main;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
	private String currentAtlasKey = new String("0001");
	private Sprite atlasSprite;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		font.setColor(Color.WHITE);
		texture = new Texture(Gdx.files.internal("units/firstUnit0.png"));
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
		Timer.schedule(new Task(){
						   @Override
						   public void run() {
							   currentFrame++;
							   if(currentFrame >= 4)
								   currentFrame = 0;

							   // ATTENTION! String.format() doesnt work under GWT for god knows why...
							   currentAtlasKey = String.format("firstUnit%d", currentFrame);
							   atlasSprite.setRegion(textureAtlas.findRegion(currentAtlasKey));
						   }
					   }
				,0,1/8.0f);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		font.draw(batch, "Player 1", 530, 710);
		font.draw(batch, "Coins: You're broke.", 940, 710);
		sprite.setPosition(Gdx.graphics.getWidth()/8, Gdx.graphics.getHeight()/8);
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

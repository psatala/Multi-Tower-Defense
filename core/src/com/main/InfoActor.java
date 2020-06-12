package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.main.Networking.requests.LeaveRoomRequest;

/**
 * This class is responsible for managing and displaying the interface for the player consisting of command buttons and gameplay information.
 * It covers everything in the game window outside the map.<br>
 * This class is only used by clients with GUIs, and not by the main server.
 * @author Piotr Libera
 */
public class InfoActor extends Actor {
    final public static float topBarHeight = 34;
    private GameManager gameManager;
    private Group infoGroup;
    private BitmapFont bigFont;
    private Pixmap pixmap;
    private Texture line;
    private Sprite lineSprite;
    private Skin skin;
    private int coins;
    private int playerId;
    private int winnerId = -1;

    /**
     * Public constructor of InfoActor
     * @param w width of the entire window
     * @param h height of the entire window
     * @param gameManager gameManager that created this object and manages the game
     * @param playerId ID of the player
     */
    public InfoActor(float w, float h, final GameManager gameManager, int playerId) {
        coins = Config.startingCoins;
        setBounds(0, h-topBarHeight, w, topBarHeight);
        this.gameManager = gameManager;
        this.playerId = playerId;
        skin = new Skin(Gdx.files.internal("uiskin.json"));
        bigFont = new BitmapFont();
        bigFont.setColor(Color.WHITE);
        pixmap = new Pixmap((int)w, 2, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        line = new Texture(pixmap);
        pixmap.dispose();
        lineSprite = new Sprite(line);
        infoGroup = new Group();
        infoGroup.addActor(this);
        addButton(20, getY()+4, 100, topBarHeight-8, "Select Units", MapActor.Mode.SELECT);
        addButton(140, getY()+4, 100, topBarHeight-8, "Move Units", MapActor.Mode.MOVE);
        addButton(260, getY()+4, 100, topBarHeight-8, "Build tower", MapActor.Mode.BUILD);
        addButton(380, getY()+4, 100, topBarHeight-8, "Spawn unit", MapActor.Mode.SPAWN);

        //add exit button
        final TextButton button = new TextButton("Exit", skin, "default");
        button.setBounds(960, getY()+4, 100, topBarHeight-8);
        infoGroup.addActor(button);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                if(!gameManager.observer.isGameOwner)
                    gameManager.observer.send(new LeaveRoomRequest(gameManager.observer.roomID));
                else
                    gameManager.observer.localServer.closeGame();

                //close connection
                gameManager.observer.isInTheGame = false;
                gameManager.observer.isGameOwner = false;
                gameManager.observer.isGameCreator = false;
                gameManager.isRunning = false;

                //set up main menu again
                gameManager.removeOtherActors();
                gameManager.menuManager.stage.addActor(gameManager.menuManager.mainTable);
            }
        });
    }

    /**
     * Overrides Actor's draw() method
     * @param batch
     * @param alpha
     */
    @Override
    public void draw(Batch batch, float alpha) {
        if(winnerId == -1)
            bigFont.draw(batch, gameManager.observer.playerName, 530, 710);
        else if(winnerId == playerId)
            bigFont.draw(batch, "You won!!!", 530, 710);
        else {
            if(winnerId == 0)
                bigFont.draw(batch, "Red Player won", 530, 710);
            else if(winnerId == 1)
                bigFont.draw(batch, "Green Player won", 530, 710);
            else if(winnerId == 2)
                bigFont.draw(batch, "Yellow Player won", 530, 710);
            else if(winnerId == 3)
                bigFont.draw(batch, "Blue Player won", 530, 710);
        }
        bigFont.draw(batch, "Coins: "+coins, 730, 710);
        lineSprite.setPosition(0, 686);
        lineSprite.draw(batch);
    }

    /**
     * Overrides Actor's act(float) method - updates the Actor's actions
     * @param delta amount of time passed since the last call of this method
     */
    @Override
    public void act(float delta) {
        super.act(delta);
    }

    /**
     * Disposes libgdx's objects
     */
    public void dispose() {
        bigFont.dispose();
        line.dispose();
    }

    /**
     * Subtracts a given number of coins from the player's account
     * @param n Number of coins to subtract
     * @return <code>true</code> if there was at least n coins before this action; <code>false</code> otherwise
     */
    public boolean spendCoins(int n) {
        if(coins < n)
            return false;
        coins -= n;
        return true;
    }

    /**
     * Adds a given number of coins to the player's account
     * @param n Number of coins to add
     */
    public void addCoins(int n) {
        coins += n;
    }

    /**
     * Creates and adds a new button to the interface. When clicked the button will set a mode to the whole interface on the gameManager's level.
     * @param x X coordinate of the bottom left corner of the button
     * @param y Y coordinate of the bottom left corner of the button
     * @param w Width of the button
     * @param h Height of the button
     * @param text Text on the button
     * @param mode Mode that the button will activate
     * @see MapActor.Mode
     */
    private void addButton(float x, float y, float w, float h, String text, final MapActor.Mode mode) {
        final TextButton button = new TextButton(text, skin, "default");
        button.setBounds(x, y, w, h);
        infoGroup.addActor(button);

        button.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                gameManager.setMode(mode);
            }
        });
    }

    /**
     * Getter method
     * @return Group object containing all the actors of the interface (PlayerInterface and Buttons)
     */
    public Group getInfoGroup() {
        return infoGroup;
    }


    /**
     * Sets the winner id. If this player has won (has this id) the information about victory will
     * be displayed instead of his name. If someone else has won, this information will also be displayed
     * instead of this player's name.
     * @param id Id of the winner
     */
    public void setWinner(int id) {
        if(winnerId == -1) {
            winnerId = id;
        }
    }
}
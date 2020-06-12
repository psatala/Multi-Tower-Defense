package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.main.Networking.GameClient;
import com.main.Networking.GameRoom;

import java.io.IOException;


/**
 * The MenuManager class is responsible for managing the main menu of the game. This includes several subsections of
 * the menu, implemented as libgdx Tables, such as: table for entering player name, main table to choose between
 * joining a game and hosting, table to specify player count of the created/hosted game and others. A reference to
 * object of this class can be found in GameManager, class responsible for managing the gameplay side.
 * @see GameManager
 * @author Piotr Satała
 */
public class MenuManager {

    public final static int PREF_BUTTON_WIDTH = 300;
    public final static int PREF_BUTTON_HEIGHT = 50;
    public final static int PREF_SMALL_BUTTON_WIDTH = 100;
    public final static int PREF_SMALL_BUTTON_HEIGHT = 30;
    private final static int PADDING = 20;

    private final GameClient gameClient;
    public final Stage stage;
    public final Skin skin;

    public final Table enterNameTable; //table for entering player name
    public final Table mainTable; //table for main part of the menu
    public final Table playerCountTable; //table to specify player count of the created/hosted game
    public Table joinGameTable; //table with available games
    public Table waitingRoomTable; //table with current players in the game, waiting for the game to start

    private int gameType = GameRoom.GLOBAL;


    /**
     * Public constructor for MenuManager class
     * @param gameClient reference to class handling networking side of the application
     * @param stage active stage from GameManager class
     * @see GameClient
     * @see GameManager
     */
    public MenuManager(final GameClient gameClient, final Stage stage) {

        this.gameClient = gameClient;
        this.stage = stage;


        skin = new Skin(Gdx.files.internal("uiskin.json"));

        enterNameTable = new Table(skin);
        mainTable = new Table(skin);
        playerCountTable = new Table(skin);
        waitingRoomTable = new Table(skin);

        stage.addActor(enterNameTable);

        //buttons for enter name table
        Label nameLabel = new Label("Enter your name", skin);
        nameLabel.setAlignment(Align.center);
        final TextField nameText = new TextField("", skin);
        nameText.setAlignment(Align.center);
        TextButton nameConfirm = new TextButton("Confirm", skin);

        //buttons for main table
        TextButton joinGameButton = new TextButton("Join Game", skin);
        TextButton createGlobalGameButton = new TextButton("Create Global Game", skin);
        TextButton hostLocalGameButton = new TextButton("Host Local Game", skin);
        TextButton quitButton = new TextButton("Quit", skin);

        //buttons for player count table
        TextButton twoPlayersButton = new TextButton("2", skin);
        TextButton threePlayersButton = new TextButton("3", skin);
        TextButton fourPlayersButton = new TextButton("4", skin);
        TextButton backToMainButton = new TextButton("Back", skin);



        //listeners

        //listener for enter name table
        nameConfirm.addListener( new ClickListener() {
           public void clicked(InputEvent inputEvent, float x, float y) {
               gameClient.playerName = nameText.getText();
               enterNameTable.remove();
               stage.addActor(mainTable);
           }
        });


        //listeners for buttons from main table
        joinGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               try {
                   mainTable.remove();
                   joinGameTable = new Table(skin);
                   stage.addActor(joinGameTable);
                   gameClient.chooseGame();
               } catch (InterruptedException | IOException e) {
                   e.printStackTrace();
               }
           }
        });

        createGlobalGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               mainTable.remove();
               stage.addActor(playerCountTable);
               gameType = GameRoom.GLOBAL;
           }
        });

        hostLocalGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               mainTable.remove();
               stage.addActor(playerCountTable);
               gameType = GameRoom.LOCAL;
           }
        });

        quitButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                Gdx.app.exit();
                gameClient.quit();
            }
        });


        //listener for buttons from player count table
        addListenerToButton(twoPlayersButton, 2);
        addListenerToButton(threePlayersButton, 3);
        addListenerToButton(fourPlayersButton, 4);

        backToMainButton.addListener( new ClickListener() {
           public void clicked(InputEvent inputEvent, float x, float y) {
               playerCountTable.remove();
               stage.addActor(mainTable);
           }
        });


        //construct enter name table
        enterNameTable.add(nameLabel).fillX().prefWidth(PREF_BUTTON_WIDTH).prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        enterNameTable.add(nameText).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        enterNameTable.add(nameConfirm).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        enterNameTable.setFillParent(true);

        //construct main table
        mainTable.add(joinGameButton).fillX().prefWidth(PREF_BUTTON_WIDTH).prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        mainTable.add(createGlobalGameButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        mainTable.add(hostLocalGameButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        mainTable.add(quitButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        mainTable.setFillParent(true);

        //construct player count table
        addLabel("Choose Number Of Players", playerCountTable, PREF_BUTTON_WIDTH, PREF_BUTTON_HEIGHT, true);
        playerCountTable.row();
        playerCountTable.add(twoPlayersButton).fillX().prefWidth(PREF_BUTTON_WIDTH).prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(threePlayersButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(fourPlayersButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(backToMainButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.setFillParent(true);

    }


    /**
     * Method adds listener to button from playerCountTable
     * @param textButton reference to clickable button with text on it
     * @param countPlayers number of players to be set as maximum number of players of a room when this button is clicked
     */
    private void addListenerToButton(TextButton textButton, final int countPlayers) {
        textButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                gameClient.maxPlayers = countPlayers;
                try {
                    if(gameType == GameRoom.LOCAL) { //local game
                        gameClient.hostLocalGame();
                        playerCountTable.remove();
                        stage.addActor(waitingRoomTable);
                    }
                    else { //global game
                        if(gameClient.createGlobalGame()) {
                            playerCountTable.remove();
                            stage.addActor(waitingRoomTable);
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * Method adds label of predefined width and height to a specified table
     * @param text text of the label
     * @param table table to add the label to
     */
    public void addLabel(String text, Table table) {
        addLabel(text, table, PREF_SMALL_BUTTON_WIDTH, PREF_SMALL_BUTTON_HEIGHT, false);
    }


    /**
     * General method for adding label to a given table
     * @param text text of the label
     * @param table table to add the label to
     * @param width width of the label
     * @param height height of the label
     * @param isCentred boolean specifying if the label should be centred
     */
    public void addLabel(String text, Table table, int width, int height, boolean isCentred) {
        Label label = new Label(text, skin);
        if(isCentred)
            label.setAlignment(Align.center);
        table.add(label).fillX().prefWidth(width).prefHeight(height);
    }
}

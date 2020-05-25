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

public class MenuManager {

    private final static int PREF_BUTTON_WIDTH = 300;
    private final static int PREF_BUTTON_HEIGHT = 50;
    public final static int PREF_SMALL_BUTTON_WIDTH = 100;
    public final static int PREF_SMALL_BUTTON_HEIGHT = 30;
    private final static int PADDING = 20;

    private final GameClient gameClient;
    public final Stage stage;
    public final Skin skin;

    public final Table enterNameTable;
    public final Table mainTable;
    public final Table playerCountTable;
    public Table joinGameTable;

    private int gameType = GameRoom.GLOBAL;

    public MenuManager(final GameClient gameClient, final Stage stage) {

        this.gameClient = gameClient;
        this.stage = stage;


        skin = new Skin(Gdx.files.internal("uiskin.json"));

        enterNameTable = new Table(skin);
        mainTable = new Table(skin);
        playerCountTable = new Table(skin);

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
                gameClient.quit();
                Gdx.app.exit();
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
        playerCountTable.add(twoPlayersButton).fillX().prefWidth(PREF_BUTTON_WIDTH).prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(threePlayersButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(fourPlayersButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.add(backToMainButton).fillX().prefHeight(PREF_BUTTON_HEIGHT).padBottom(PADDING).row();
        playerCountTable.setFillParent(true);

    }

    private void addListenerToButton(TextButton textButton, final int countPlayers) {
        textButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                gameClient.maxPlayers = countPlayers;
                try {
                    if(gameType == GameRoom.LOCAL) { //local game
                        gameClient.hostLocalGame();
                        playerCountTable.remove();
                        gameClient.gameManager.addOtherActors();
                    }
                    else { //global game
                        if(gameClient.createGlobalGame()) {
                            playerCountTable.remove();
                            gameClient.gameManager.addOtherActors();
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void addLabel(String text, Table table) {
        Label label = new Label(text, skin);
        table.add(label).fillX().prefWidth(PREF_SMALL_BUTTON_WIDTH).prefHeight(PREF_SMALL_BUTTON_HEIGHT);
    }
}

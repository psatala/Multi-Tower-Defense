package com.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.main.Networking.GameClient;

import java.io.IOException;

public class MenuManager {

    private final GameClient gameClient;
    private final Stage stage;
    private final Skin skin;
    public final Table mainTable;
    public final Table playerCountTable;

    public MenuManager(final GameClient gameClient, final Stage stage) {

        this.gameClient = gameClient;
        this.stage = stage;


        skin = new Skin(Gdx.files.internal("uiskin.json"));
        mainTable = new Table(skin);
        playerCountTable = new Table(skin);

        stage.addActor(mainTable);

        //buttons for main table
        TextButton joinGameButton = new TextButton("Join Game", skin);
        TextButton createGlobalGameButton = new TextButton("Create Global Game", skin);
        TextButton hostLocalGameButton = new TextButton("Host Local Game", skin);
        TextButton quitButton = new TextButton("Quit", skin);

        //buttons for player count table
        TextButton twoPlayersButton = new TextButton("2", skin);
        TextButton threePlayersButton = new TextButton("3", skin);
        TextButton fourPlayersButton = new TextButton("4", skin);


        //listeners for buttons from main table
        joinGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               try {
                   gameClient.joinGame();
               } catch (InterruptedException | IOException e) {
                   e.printStackTrace();
               }
           }
        });

        createGlobalGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               try {
                   gameClient.createGlobalGame();
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }
           }
        });

        hostLocalGameButton.addListener( new ClickListener() {
           @Override
           public void clicked(InputEvent inputEvent, float x, float y) {
               mainTable.remove();
               stage.addActor(playerCountTable);
           }
        });

        quitButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                gameClient.quit();
            }
        });


        //listener for buttons from player count table
        addListenerToButton(twoPlayersButton, 2);
        addListenerToButton(threePlayersButton, 3);
        addListenerToButton(fourPlayersButton, 4);

        //construct main table
        mainTable.add(joinGameButton).fillX().row();
        mainTable.add(createGlobalGameButton).fillX().row();
        mainTable.add(hostLocalGameButton).fillX().row();
        mainTable.add(quitButton).fillX().row();
        mainTable.setFillParent(true);

        //construct player count table
        playerCountTable.add(twoPlayersButton).fillX();
        playerCountTable.add(threePlayersButton).fillX();
        playerCountTable.add(fourPlayersButton).fillX();
        playerCountTable.setFillParent(true);

    }

    private void addListenerToButton(TextButton textButton, final int countPlayers) {
        textButton.addListener( new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                gameClient.maxPlayers = countPlayers;
                playerCountTable.remove();
                gameClient.gameManager.addOtherActors();
                try {
                    gameClient.hostLocalGame();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}

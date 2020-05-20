
package com.main.Networking;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Client;

import com.main.GameManager;
import com.main.Networking.requests.*;
import com.main.Networking.responses.*;


/**
 The GameClient class is the main class to be run by the user. It handles connecting with main server,
 creating global games and hosting local games by starting a local server.
 @author Piotr Satała
 */
public class GameClient {


    private final Client client;
    private final Client localClient;
    private Client activeClient;
    private LocalServer localServer;
    private final Scanner inputScanner;
    public String playerName;
    public int roomID;
    public int maxPlayers;
    private final RoomList roomList;
    public GameManager gameManager;
    public UpdatesListener updatesListener;
    private boolean isGameOwner = false;


    private final int tcpSecondPortNumber;
    private final int udpSecondPortNumber;
    private final int maxDelay;

    /**
     * Public constructor for GameClient class
     * @param tcpPortNumber tcp port of main server
     * @param udpPortNumber udp port of main server
     * @param tcpSecondPortNumber tcp port to host local games on
     * @param udpSecondPortNumber udp port to host local games on
     * @param maxDelay timeout in milliseconds for connecting and host discovery
     */
    public GameClient(int tcpPortNumber, int udpPortNumber, int tcpSecondPortNumber, int udpSecondPortNumber,
            int maxDelay) throws IOException, InterruptedException {

        this.tcpSecondPortNumber = tcpSecondPortNumber;
        this.udpSecondPortNumber = udpSecondPortNumber;
        this.maxDelay = maxDelay;

        client = new Client();
        localClient = new Client();
        roomList = new RoomList();

        // register classes
        Network.register(client);
        Network.register(localClient);

        gameManager = new GameManager(0);
        gameManager.addObserver(this);

        updatesListener = new UpdatesListener() {
            @Override
            public void updatesPending(Object object, int roomID) {
                send(object);
            }
        };



        // listener for client connected to the global server
        client.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { //response with game data
                    GameResponse gameResponse = (GameResponse) object;
                    if(gameManager != null)
                        gameManager.getUpdates(gameResponse);

                } else if(object instanceof RewardResponse) { //response with rewards
                    RewardResponse rewardResponse = (RewardResponse)object;
                    if(gameManager != null)
                        gameManager.getRewards(rewardResponse);

                } else if (object instanceof ControlResponse) { //control response
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof RoomList) { //list of available rooms on global server
                    RoomList mainServerRoomList = (RoomList) object;
                    roomList.putALL(mainServerRoomList);
                    synchronized(client) {
                        client.notify();
                    }

                } else if (object instanceof RoomCreatedResponse) { //room successfully created
                    RoomCreatedResponse roomCreatedResponse = (RoomCreatedResponse) object;
                    roomID = roomCreatedResponse.roomID;
                    gameManager.setPlayerId(0);
                    synchronized(client) {
                        client.notify();
                    }
                } else if (object instanceof RoomJoinedResponse) { //room successfully joined
                    RoomJoinedResponse roomJoinedResponse = (RoomJoinedResponse)object;
                    gameManager.setPlayerId(roomJoinedResponse.getIdWithinRoom());
                    synchronized(client) {
                        client.notify();
                    }
                }
            }
        });

        
        
        //listener for client connected to local server
        localClient.addListener(new Listener() {
            public void received(Connection connection, Object object) {
                if (object instanceof GameResponse) { //response with game data
                    GameResponse gameResponse = (GameResponse) object;
                    if(gameManager != null)
                        gameManager.getUpdates(gameResponse);

                } else if(object instanceof RewardResponse) { //response with rewards
                    RewardResponse rewardResponse = (RewardResponse)object;
                    if(gameManager != null)
                        gameManager.getRewards(rewardResponse);

                } else if (object instanceof ControlResponse) { //control response
                    ControlResponse controlResponse = (ControlResponse) object;
                    System.out.println(controlResponse.getMessage());

                } else if (object instanceof GameRoom) { //room available on local server
                    GameRoom gameRoom = (GameRoom) object;
                    gameRoom.ipOfHost = connection.getRemoteAddressTCP().getAddress();
                    if(roomList.containsKey(gameRoom.roomID)) //if key already in list
                        gameRoom.roomID = roomList.getMaxKey() + 1; //change key so it is unique - keys are not important to local servers 
                    
                    roomList.add(gameRoom);
                    synchronized(localClient) {
                        localClient.notify();
                    }

                } else if (object instanceof RoomJoinedResponse) { //room successfully joined
                    RoomJoinedResponse roomJoinedResponse = (RoomJoinedResponse)object;
                    gameManager.setPlayerId(roomJoinedResponse.getIdWithinRoom());
                    synchronized(localClient) {
                        localClient.notify();
                    }
                } else if (object instanceof RoomClosedResponse) { //room closed
                    localClient.close();
                }
                
            }
        });


        //start connection to main server
        client.start();

        try { //to connect with main server from the outside

            //to change the address of the main server, simply change the second argument of the following
            //function into whatever domain name or IP address you desire
            client.connect(maxDelay, "multitowerdefense.hopto.org", tcpPortNumber, udpPortNumber);
            //TODO: fix incorrect timeout
        }
        catch(IOException e1) {
            String cannotConnectError = "Connection to main server could not be established";
            InetAddress mainServerAddress = client.discoverHost(udpPortNumber, maxDelay); //discover host on LAN
            if(mainServerAddress == null)
                System.out.println(cannotConnectError);
            else {
                try { //to connect with main server from the inside
                    client.connect(maxDelay, mainServerAddress, tcpPortNumber, udpPortNumber);
                }
                catch(IOException e2){ //could not connect
                    System.out.println(cannotConnectError);
                }
            }
        }
        
        //start connection to local server
        localClient.start();


        //scanner for input
        inputScanner = new Scanner (System.in);

        //get name
        System.out.println("Enter your name");
        playerName = inputScanner.nextLine();




    }


    /**
     * Menu for client which allows for:
     * * searching and joining global and local rooms(games)
     * * creating global rooms(games) hosted by the main server
     * * hosting local rooms(games)
     * * quitting
     * @throws InterruptedException
     * @throws IOException
     */
    public void menu() throws InterruptedException, IOException {

        String input;
        inputScanner.reset();
        //while(true)
        {
            System.out.println("Enter 'j' to join a room, 'c' to create a global room, 'h' to host a local room, 'q' to quit");
            input = inputScanner.nextLine();

            switch (input) {
                case "j":
                    chooseGame();
                    break;
                case "c":
                    createGlobalGame();
                    break;
                case "h":
                    hostLocalGame();
                    break;
                case "q":
                    quit();
                    //break;
                    break;
            }
        }
        
    }



    /**
     * Allow user to join global or local games
     * @throws InterruptedException
     * @throws IOException
     */
    public void chooseGame() throws InterruptedException, IOException {
        
        List<InetAddress> hostList;
        final ArrayList<Integer> arrayOfKeys;
        final int[] roomNumber = new int[1];
        TextButton textButton;
        int roomIndex = 0;

        if(client.isConnected()) { //if connection to global server is established
            synchronized(client) {
                client.sendTCP(new GetRoomListRequest());
                client.wait();
            }
        }

        //TODO: limit interfaces to one, use MAC address?
        //search for hosts on LAN
        hostList = localClient.discoverHosts(udpSecondPortNumber, maxDelay);
        for(InetAddress host: hostList) {
            localClient.connect(maxDelay, host, tcpSecondPortNumber, udpSecondPortNumber);
            synchronized(localClient) {
                localClient.sendTCP(new GetRoomInfoRequest()); //get info about room from every host on LAN
                localClient.wait();
            }
            localClient.close();
        }


        arrayOfKeys = roomList.getArrayOfKeys();


        //add buttons for each room
        for(int roomItemID: arrayOfKeys) {
            textButton = new TextButton(roomList.get(roomItemID).printRoomInfo(), gameManager.menuManager.skin);
            final int finalRoomIndex = roomIndex;
            textButton.addListener(new ClickListener() {
               @Override
               public void clicked(InputEvent inputEvent, float x, float y) {
                   roomNumber[0] = finalRoomIndex;
                   gameManager.menuManager.joinGameTable.remove();
                   gameManager.addOtherActors();
                   try {
                       joinGame(roomNumber[0], arrayOfKeys);
                   } catch (InterruptedException | IOException e) {
                       e.printStackTrace();
                   }
               }
            });
            gameManager.menuManager.joinGameTable.add(textButton).fillX().row();
            ++roomIndex;
        }


        //add go back button
        textButton = new TextButton("Back", gameManager.menuManager.skin);
        textButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent inputEvent, float x, float y) {
                roomNumber[0] = -1;
                roomList.clear();
                gameManager.menuManager.joinGameTable.remove();
                gameManager.menuManager.stage.addActor(gameManager.menuManager.mainTable);
            }
        });
        gameManager.menuManager.joinGameTable.add(textButton).fillX().row();

        //fill stage with table
        gameManager.menuManager.joinGameTable.setFillParent(true);
        
    }

    public void joinGame(int roomNumber, ArrayList<Integer> arrayOfKeys) throws InterruptedException, IOException {
        roomID = arrayOfKeys.get(roomNumber);
        if(roomList.get(roomID).gameType == GameRoom.GLOBAL) { //if room is global
            activeClient = client;
            synchronized(client) {
                client.sendTCP(new JoinRoomRequest(roomID));
                client.wait();
            }
        }
        else { //if room is local
            activeClient = localClient;
            localClient.connect(maxDelay, roomList.get(roomID).ipOfHost, tcpSecondPortNumber, udpSecondPortNumber);
            localClient.sendTCP(new JoinRoomRequest());
        }

        roomList.clear();
    }


    
    /**
     * Allow user to create global games hosted by the main server
     * @throws InterruptedException
     */
    public boolean createGlobalGame() throws InterruptedException {

            if(client.isConnected()) { //connection to the main server must be established
                activeClient = client;
                //request to create a room
                CreateRoomRequest createRoomRequest = new CreateRoomRequest(playerName, maxPlayers, GameRoom.GLOBAL);
                synchronized(client) {
                    //send and wait for response
                    client.sendTCP(createRoomRequest);
                    client.wait();
                }
                return true;
            }
            System.out.println("This option requires connection to the main server");
            return false;
    }



    /**
     * Allow user to host local games - only one game can be hosted on each computer
     * @throws IOException
     * @throws InterruptedException
     */
    public void hostLocalGame() throws IOException {

        gameManager.setPlayerId(0);
        localServer = new LocalServer(tcpSecondPortNumber, udpSecondPortNumber, playerName, maxPlayers, gameManager);
        isGameOwner = true;
    }



    /**
     * Quit from the game
     */
    public void quit() {
        client.close();
        client.stop();
        localClient.close();
        localClient.stop();
        isGameOwner = false;
    }





    /**
     * After joining or creating a room, send request with client updates
     */
    public void send(Object object) {

        if(!isGameOwner) {
            if(activeClient.isConnected())
                activeClient.sendTCP(object);
        }
        else
            localServer.superManager.getUpdates((GameRequest)object);

    }
    
}
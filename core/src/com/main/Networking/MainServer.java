
package com.main.Networking;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.main.Networking.requests.*;
import com.main.Networking.responses.*;
import com.main.SuperManager;

import java.io.IOException;
import java.util.*;

/**
 * The GameServer class is the core class controlling the main server. There should be at most one main server
 * running at once and its IP address should be specified in GameClient class.
 * @see GameServer
 * @see GameClient
 * @author Piotr Satała
 */
public class MainServer extends GameServer {

    /**
     * Rate at which local server checks connection status
     */
    private static final int CHECK_CONNECTION_RATE = 1000;

    private final RoomList roomList = new RoomList();
    private HashMap<Integer, SuperManager> managerHashMap; //hashmap with super gameplay managers

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public MainServer() {}


    /**
     * Public constructor for GameServer class
     * @param tcpPortNumber tcp port for main server
     * @param udpPortNumber udp port for main server
     * @throws IOException binding the server was not successful
     */
    public MainServer(int tcpPortNumber, int udpPortNumber) throws IOException {
        super();

        //hashmap with managers
        managerHashMap = new HashMap<>();

        //register classes
        Network.register(this);



        //add listener
        addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
                if(object instanceof GameRequest) { //request with game data
                    
                    GameRequest gameRequest = (GameRequest)object;
                    managerHashMap.get(gameRequest.getRoomID()).getUpdates(gameRequest); //apply updates

                }
                else if(object instanceof CreateRoomRequest) { //client wants to create a room
                    
                    CreateRoomRequest createRoomRequest = (CreateRoomRequest)object;
                    GameRoom newRoom = new GameRoom(createRoomRequest.hostName, createRoomRequest.maxPlayers, createRoomRequest.gameType, connection.getID(), createRoomRequest.hostName);
                    managerHashMap.put(GameRoom.getLastRoomID(), new SuperManager());
                    managerHashMap.get(GameRoom.getLastRoomID()).roomID = GameRoom.getLastRoomID(); //specify id of room
                    managerHashMap.get(GameRoom.getLastRoomID()).addObserver(MainServer.this); //add observer to newly created manager
                    roomList.add(newRoom); //add to list of rooms
                    RoomCreatedResponse roomCreatedResponse = new RoomCreatedResponse(newRoom.roomID);
                    sendToTCP(connection.getID(), roomCreatedResponse); //inform about successful creation

                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room
                    
                    JoinRoomRequest joinRoomRequest = (JoinRoomRequest)object;
                    GameRoom currentRoom = roomList.get(joinRoomRequest.roomID); //get id
                    try {
                        currentRoom.addPlayer(connection.getID(), joinRoomRequest.playerName);
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse(currentRoom.currentPlayers - 1);
                        sendToTCP(connection.getID(), roomJoinedResponse); //room successfully joined
                    }
                    catch(Exception e) { //room already full
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof LeaveRoomRequest) { //client wants to leave a room
                    LeaveRoomRequest leaveRoomRequest = (LeaveRoomRequest)object;
                    GameRoom currentRoom = roomList.get(leaveRoomRequest.roomID);

                    try {
                        currentRoom.removePlayer(connection.getID());
                    }
                    catch(Exception e) { //room now empty
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof GetRoomListRequest) { //client wants to get a list of available rooms
                    sendToTCP(connection.getID(), roomList);
                }
                else if(object instanceof StartGameRequest) { //client (game creator) wants to start the game
                    StartGameRequest startGameRequest = (StartGameRequest) object;
                    startGame(startGameRequest.getRoomID());
                }
            }
        });


        //start
        start();
        bind(tcpPortNumber, udpPortNumber);


        //create new timer which updates count of players connected and sends them a list of names of connected players
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updatePlayerCount();
                sendListOfNames();
            }
        }
            , 0, CHECK_CONNECTION_RATE);
    }



    /**
     * Method responsible for sending game updates and rewards to clients
     * @param object object to be sent to clients
     * @param roomID id of the room the data refers to
     */
    @Override
    public void send(Object object, int roomID) {
        for(NamePair connectionID: roomList.get(roomID).connectionSet)
            sendToTCP(connectionID.getKey(), object);
    }



    /**
     * Method checks which players are still connected to the main server
     * and updates number of current players.
     */
    private void updatePlayerCount() {
        ArrayList<Integer> arrayOfKeys = roomList.getArrayOfKeys();
        HashSet<Integer> tempSet;
        for(Integer key: arrayOfKeys) {
            GameRoom gameRoom = roomList.get(key);
            gameRoom.currentPlayers = 0;
            tempSet = new HashSet<>();
            for(NamePair namePair: gameRoom.connectionSet) {
                tempSet.add(namePair.getKey());
            }
            Connection[] connections = getConnections();
            for(Connection connection: connections) {
                if(connection.isConnected() && tempSet.contains(connection.getID()))
                    ++gameRoom.currentPlayers;
            }
        }
    }



    /**
     * Method sends list of names of all players connected to a given game. This list is
     * sent those connected players. This only applies when game is in the waiting room phase.
     */
    private void sendListOfNames() {
        ArrayList<Integer> arrayList = roomList.getArrayOfKeys(); //get array with room ids
        for(Integer key: arrayList) { //for each room
            GameRoom gameRoom = roomList.get(key);
            if(!gameRoom.isRunning) { //if in waiting room phase
                NameListResponse nameListResponse = new NameListResponse();
                for(NamePair namePair : gameRoom.connectionSet) { //construct list
                    nameListResponse.arrayList.add(namePair.getValue());
                }
                for(NamePair connectionID: gameRoom.connectionSet) { //send list
                        sendToTCP(connectionID.getKey(), nameListResponse);
                }
            }
        }
    }



    /**
     * Method called when creator of the game clicks the "Start Game" button. It informs all other
     * players that the game has started.
     * @param roomID id of the room the game creator is in
     */
    public void startGame(int roomID) {
        GameRoom gameRoom = roomList.get(roomID);
        gameRoom.isRunning = true;
        for(NamePair namePair: gameRoom.connectionSet) {
            sendToTCP(namePair.getKey(), new StartGameResponse());
        }
    }
}

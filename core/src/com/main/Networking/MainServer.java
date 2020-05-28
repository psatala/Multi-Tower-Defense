
package com.main.Networking;


import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.main.Networking.requests.*;
import com.main.Networking.responses.ControlResponse;
import com.main.Networking.responses.RoomCreatedResponse;
import com.main.Networking.responses.RoomJoinedResponse;
import com.main.SuperManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The GameServer class is the core class controlling the main server. There should be at most one main server
 * running at once and its IP address should be specified in GameClient class.
 * @author Piotr Satała
 */
public class MainServer extends GameServer {

    private final RoomList roomList = new RoomList();
    private HashMap<Integer, SuperManager> managerHashMap;
    private static final int CHECK_CONNECTION_RATE = 1000;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public MainServer() {}


    /**
     * Public constructor for GameServer class
     * @param tcpPortNumber tcp port for main server
     * @param udpPortNumber udp port for main server
     * @throws IOException
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
                    GameRoom newRoom = new GameRoom(createRoomRequest.hostName, createRoomRequest.maxPlayers, createRoomRequest.gameType, connection.getID());
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
                        currentRoom.addPlayer(connection.getID());
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
                        roomList.remove(currentRoom.roomID);
                    }
                }
                else if(object instanceof GetRoomListRequest) { //client wants to get a list of available rooms
                    sendToTCP(connection.getID(), roomList);
                }
            }
        });


        //start
        start();
        bind(tcpPortNumber, udpPortNumber);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                updatePlayerCount();
            }
        }
            , 0, CHECK_CONNECTION_RATE);
    }



    @Override
    public void send(Object object, int roomID) {
        for(Integer connectionID: roomList.get(roomID).connectionSet)
            sendToTCP(connectionID, object);
    }


    private void updatePlayerCount() {
        ArrayList<Integer> arrayOfKeys = roomList.getArrayOfKeys();
        for(Integer key: arrayOfKeys) {
            GameRoom gameRoom = roomList.get(key);
            gameRoom.currentPlayers = 0;
            Connection[] connections = getConnections();
            for(Connection connection: connections) {
                if(connection.isConnected() && gameRoom.connectionSet.contains(connection.getID()))
                    ++gameRoom.currentPlayers;
            }
        }
    }
}


package com.main.Networking;

import java.io.IOException;
import java.util.HashMap;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import com.main.Networking.requests.*;
import com.main.Networking.responses.*;
import com.main.SuperManager;

/**
 * The GameServer class is the core class controlling the main server. There should be at most one main server
 * running at once and its IP address should be specified in GameClient class.
 * @author Piotr Satała
 */
public class GameServer {
    private Server server;
    private RoomList roomList = new RoomList();
    public UpdatesListener updatesListener;
    private HashMap<Integer, SuperManager> managerHashMap;
    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameServer() {}

    /**
     * Public constructor for GameServer class
     * @param tcpPortNumber tcp port for main server
     * @param udpPortNumber udp port for main server
     * @throws IOException
     */
    public GameServer(int tcpPortNumber, int udpPortNumber) throws IOException {
        server = new Server();

        //hashmap with managers
        managerHashMap = new HashMap<>();

        //register classes
        Network.register(server);

        updatesListener = new UpdatesListener() {
            @Override
            public void updatesReceived(Object object) {

            }

            @Override
            public void updatesPending(Object object, int roomID) {
                send(object, roomID);
            }
        };


        //add listener
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
                if(object instanceof GameRequest) { //request with game data
                    
                    GameRequest gameRequest = (GameRequest)object;
                    if(gameRequest != null)
                        managerHashMap.get(gameRequest.getRoomID()).getUpdates(gameRequest); //apply updates

                }
                else if(object instanceof CreateRoomRequest) { //client wants to create a room
                    
                    CreateRoomRequest createRoomRequest = (CreateRoomRequest)object;
                    GameRoom newRoom = new GameRoom(createRoomRequest.hostName, createRoomRequest.maxPlayers, createRoomRequest.gameType, connection.getID());
                    managerHashMap.put(GameRoom.getLastRoomID(), new SuperManager());
                    managerHashMap.get(GameRoom.getLastRoomID()).roomID = GameRoom.getLastRoomID(); //specify id of room
                    managerHashMap.get(GameRoom.getLastRoomID()).addObserver(GameServer.this); //add observer to newly created manager
                    roomList.add(newRoom); //add to list of rooms
                    RoomCreatedResponse roomCreatedResponse = new RoomCreatedResponse(newRoom.roomID);
                    server.sendToTCP(connection.getID(), roomCreatedResponse); //inform about successful creation

                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room
                    
                    JoinRoomRequest joinRoomRequest = (JoinRoomRequest)object;
                    GameRoom currentRoom = roomList.get(joinRoomRequest.roomID); //get id
                    try {
                        currentRoom.addPlayer(connection.getID());
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse();
                        server.sendToTCP(connection.getID(), roomJoinedResponse); //room successfully joined
                    }
                    catch(Exception e) { //room already full
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        server.sendToTCP(connection.getID(), controlResponse);
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
                        server.sendToTCP(connection.getID(), controlResponse);
                        roomList.remove(currentRoom.roomID);
                    }
                }
                else if(object instanceof GetRoomListRequest) { //client wants to get a list of available rooms
                    server.sendToTCP(connection.getID(), roomList);
                }
            }
        });
        

        //start
        server.start();
        server.bind(tcpPortNumber, udpPortNumber);

    }



    public void send(Object object, int roomID) {
        for(Integer connectionID: roomList.get(roomID).connectionSet)
            server.sendToTCP(connectionID, object);
    }
}

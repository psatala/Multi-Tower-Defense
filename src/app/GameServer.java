package app;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import app.requests.*;
import app.responses.*;

public class GameServer {
    private Server server;
    private RoomList roomList = new RoomList();
    
    public GameServer(int tcpPortNumber) throws IOException {
        server = new Server();
        
        //register classes
        Kryo kryo = server.getKryo();
        //requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        //responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(HashMap.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);

        
        //add listener
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                if(object instanceof GameRequest) {
                    GameRequest gameRequest = (GameRequest)object;
                    GameResponse gameResponse = new GameResponse(gameRequest.getMessage());
                    GameRoom currentRoom = roomList.get(gameRequest.getRoomID());
                    if(currentRoom != null)
                        for(Integer connectionID: currentRoom.connectionSet)
                            if(connectionID != connection.getID())
                                server.sendToTCP(connectionID, gameResponse);
                    
                }
                else if(object instanceof CreateRoomRequest) { //client wants to create a room
                    
                    CreateRoomRequest createRoomRequest = (CreateRoomRequest)object;
                    GameRoom newRoom = new GameRoom(createRoomRequest.hostName, createRoomRequest.maxPlayers, createRoomRequest.gameType, connection.getID());
                    roomList.add(newRoom);

                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room
                    
                    JoinRoomRequest joinRoomRequest = (JoinRoomRequest)object;
                    GameRoom currentRoom = roomList.get(joinRoomRequest.roomID);
                    ControlResponse controlResponse = null;
                    try {
                        currentRoom.addPlayer(connection.getID());
                        controlResponse = new ControlResponse("Room joined");
                    }
                    catch(Exception e) {
                        controlResponse = new ControlResponse(e.getMessage());
                    }
                    finally {
                        server.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof LeaveRoomRequest) { //client wants to leave a room
                    LeaveRoomRequest leaveRoomRequest = (LeaveRoomRequest)object;
                    GameRoom currentRoom = roomList.get(leaveRoomRequest.roomID);

                    try {
                        currentRoom.removePlayer(connection.getID());
                    }
                    catch(Exception e) {
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        server.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof GetRoomListRequest) { //client wants to get a list of available rooms
                    server.sendToTCP(connection.getID(), roomList);
                }
            }
        });
        

        //start
        server.start();
        server.bind(tcpPortNumber);

    }

}
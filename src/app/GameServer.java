package app;

import java.io.IOException;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import app.requests.*;
import app.responses.*;

public class GameServer {
    private Server server;
    private RoomList roomList = new RoomList();
    
    public GameServer(int tcpPortNumber, int udpPortNumber) throws IOException {
        server = new Server();
        
        //register classes
        Network.register(server);

        //add listener
        server.addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
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
                    RoomCreatedResponse roomCreatedResponse = new RoomCreatedResponse(newRoom.roomID);
                    server.sendToTCP(connection.getID(), roomCreatedResponse);

                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room
                    
                    JoinRoomRequest joinRoomRequest = (JoinRoomRequest)object;
                    GameRoom currentRoom = roomList.get(joinRoomRequest.roomID);
                    try {
                        currentRoom.addPlayer(connection.getID());
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse();
                        server.sendToTCP(connection.getID(), roomJoinedResponse);
                    }
                    catch(Exception e) {
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
                    catch(Exception e) {
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

}
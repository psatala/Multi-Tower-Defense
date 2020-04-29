package app;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import app.requests.*;
import app.responses.*;

public class LocalGameServer {
    private Server localServer;
    private GameRoom gameRoom;
    
    public LocalGameServer(int tcpPortNumber, int udpPortNumber, String hostName, int maxPlayers) throws IOException {
        localServer = new Server();
        gameRoom = new GameRoom(hostName, maxPlayers, GameRoom.LOCAL, -1); //add local server to game room
        gameRoom.ipOfHost = InetAddress.getLocalHost();

        //register classes
        Kryo kryo = localServer.getKryo();
        //requests
        kryo.register(GameRequest.class);
        kryo.register(CreateRoomRequest.class);
        kryo.register(JoinRoomRequest.class);
        kryo.register(LeaveRoomRequest.class);
        kryo.register(GetRoomListRequest.class);
        kryo.register(GetRoomInfoRequest.class);
        //responses
        kryo.register(GameResponse.class);
        kryo.register(ControlResponse.class);
        kryo.register(RoomList.class);
        kryo.register(GameRoom.class);
        kryo.register(HashSet.class);
        kryo.register(RoomCreatedResponse.class);
        kryo.register(RoomJoinedResponse.class);

        //add listener
        localServer.addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
                if(object instanceof GameRequest) {
                    GameRequest gameRequest = (GameRequest)object;

                    System.out.println(gameRequest.getMessage());

                    GameResponse gameResponse = new GameResponse(gameRequest.getMessage());
                    if(gameRoom != null)
                        for(Integer connectionID: gameRoom.connectionSet)
                            if(connectionID != connection.getID() && connectionID != -1)
                                localServer.sendToTCP(connectionID, gameResponse);
                    
                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room

                    try {
                        gameRoom.addPlayer(connection.getID());
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse();
                        localServer.sendToTCP(connection.getID(), roomJoinedResponse);
                    }
                    catch(Exception e) {
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        localServer.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof LeaveRoomRequest) { //client wants to leave a room
                    
                    try {
                        gameRoom.removePlayer(connection.getID());
                    }
                    catch(Exception e) {
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        localServer.sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof GetRoomInfoRequest) { //client wants to get info about the room
                    localServer.sendToTCP(connection.getID(), gameRoom);
                }
            }
        });
        

        //start
        localServer.start();
        localServer.bind(tcpPortNumber, udpPortNumber);

    }

}
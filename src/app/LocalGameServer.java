package app;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import app.requests.*;
import app.responses.*;

public class LocalGameServer {
    private Server localServer;
    private GameRoom gameRoom;
    private Scanner inputScanner;

    public LocalGameServer(int tcpPortNumber, int udpPortNumber, int maxDelay, String hostName, int maxPlayers, Scanner inputScanner) 
    throws IOException, InterruptedException {
        localServer = new Server();
        gameRoom = new GameRoom(hostName, maxPlayers, GameRoom.LOCAL, -1); //add local server to game room

        this.inputScanner = inputScanner;

        //register classes
        Network.register(localServer);

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
                        connection.close();
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
        try {
            localServer.bind(tcpPortNumber, udpPortNumber);
            run(tcpPortNumber, udpPortNumber, maxDelay);
        }
        catch(BindException e) {
            System.out.println("Another server is already running on this computer");
            //TODO: fix incorrect quitting after this exception
        }

        
    }



    public void run(int tcpSecondPortNumber, int udpSecondPortNumber, int maxDelay) throws InterruptedException, IOException {
        
        System.out.println("Press 'q' to quit");

        GameResponse gameResponse = new GameResponse();
        while(true) {
            gameResponse.setMessage(inputScanner.nextLine());
            if(gameResponse.getMessage().equals("q")) { //quitting
                for(Integer connectionID: gameRoom.connectionSet)
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, new RoomClosedResponse());
                localServer.close();
                localServer.stop();
                break;
            }
            else {
                for(Integer connectionID: gameRoom.connectionSet) //send data to everyone
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, gameResponse);
            }
        }
    }
    
}
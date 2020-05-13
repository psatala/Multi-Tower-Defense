
package com.main.Networking;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

import com.badlogic.gdx.Game;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import com.main.GameManager;
import com.main.Networking.requests.*;
import com.main.Networking.responses.*;
import com.main.SuperManager;

/**
 * The LocalServer class allows user to host a server accessible on his LAN. The local server uses a port
 * different to the one used by the main server.
 * @author Piotr Satała
 */
public class LocalServer extends GameServer {
    private GameRoom gameRoom;
    private Scanner inputScanner;

    public SuperManager superManager;
    private GameManager gameOwner;

    /**
     * Public constructor for LocalServer class
     * @param tcpSecondPortNumber tcp port for local server
     * @param udpSecondPortNumber udp port for local server
     * @param hostName name of user who is hosting the local server
     * @param maxPlayers max number of players on server
     * @param inputScanner input scanner for host
     * @throws IOException
     * @throws InterruptedException
     */
    public LocalServer(int tcpSecondPortNumber, int udpSecondPortNumber, String hostName,
                       int maxPlayers, Scanner inputScanner, GameManager gameOwner) throws IOException, InterruptedException {

        super();

        superManager = new SuperManager();
        superManager.addObserver(this);

        gameRoom = new GameRoom(hostName, maxPlayers, GameRoom.LOCAL, -1); //add host to game room

        this.inputScanner = inputScanner;
        this.gameOwner = gameOwner;

        //register classes
        Network.register(this);

        //add listener
        addListener(new Listener() {
            public void received(Connection connection, Object object) { //client sends a game message
                connection.setTimeout(0); //never timeout - TODO: find proper solution for connection timeout
                if(object instanceof GameRequest) { //request with game data
                    GameRequest gameRequest = (GameRequest)object;
                    superManager.getUpdates(gameRequest);
                }
                else if(object instanceof JoinRoomRequest) { //client wants to join a room

                    try {
                        gameRoom.addPlayer(connection.getID());
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse();
                        sendToTCP(connection.getID(), roomJoinedResponse);
                    }
                    catch(Exception e) { //room full
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof LeaveRoomRequest) { //client wants to leave a room
                    
                    try {
                        connection.close();
                        gameRoom.removePlayer(connection.getID());
                    }
                    catch(Exception e) { //room empty
                        ControlResponse controlResponse = new ControlResponse(e.getMessage());
                        sendToTCP(connection.getID(), controlResponse);
                    }
                }
                else if(object instanceof GetRoomInfoRequest) { //client wants to get info about the room
                    sendToTCP(connection.getID(), gameRoom);
                }
            }
        });
        

        //start
        start();
        try {
            bind(tcpSecondPortNumber, udpSecondPortNumber);
        }
        catch(BindException e) { //another server already running on this computer
            System.out.println("Another server is already running on this computer");
            //TODO: fix incorrect quitting after this exception
        }

        
    }

    @Override
    protected void send(Object object, int roomID) {
        for(Integer connectionID: gameRoom.connectionSet) {
            if(connectionID != -1)
                sendToTCP(connectionID, object);
            else if(object instanceof GameResponse)
                gameOwner.getUpdates((GameResponse)object);
            else
                gameOwner.getRewards((RewardResponse)object);
        }
    }


    /**
     * After creating the room, run the game in it
     * @throws InterruptedException
     * @throws IOException
     */
    /*public void run() throws InterruptedException, IOException {
        
        System.out.println("Press 'q' to quit");

        GameResponse gameResponse = new GameResponse(); //new response to other clients
        while(true) {
            gameResponse.setMessage(inputScanner.nextLine());
            if(gameResponse.getMessage().equals("q")) { //quitting
                for(Integer connectionID: gameRoom.connectionSet) //to all connected inform about room closing
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, new RoomClosedResponse());
                localServer.close(); //close the room
                localServer.stop();
                break;
            }
            else {
                for(Integer connectionID: gameRoom.connectionSet) //send data to everyone connected apart from host
                    if(connectionID != -1)
                        localServer.sendToTCP(connectionID, gameResponse);
            }
        }
    }*/
    
}


package com.main.Networking;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.main.GameManager;
import com.main.Networking.requests.*;
import com.main.Networking.responses.*;
import com.main.SuperManager;

import java.io.IOException;
import java.net.BindException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The LocalServer class allows user to host a server accessible on his LAN. The local server uses a port
 * different to the one used by the main server.
 * @author Piotr Satała
 */
public class LocalServer extends GameServer {
    private final GameRoom gameRoom;
    private static final int CHECK_CONNECTION_RATE = 1000;
    public SuperManager superManager;
    private final GameManager gameOwner;


    /**
     * Public constructor for LocalServer class
     * @param tcpSecondPortNumber tcp port for local server
     * @param udpSecondPortNumber udp port for local server
     * @param hostName name of user who is hosting the local server
     * @param maxPlayers max number of players on server
     * @throws IOException
     */
    public LocalServer(int tcpSecondPortNumber, int udpSecondPortNumber, String hostName,
                       int maxPlayers, final GameManager gameOwner) throws IOException {

        super();

        superManager = new SuperManager();
        superManager.addObserver(this);

        gameRoom = new GameRoom(hostName, maxPlayers, GameRoom.LOCAL, -1, gameOwner.observer.playerName); //add host to game room

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
                    JoinRoomRequest joinRoomRequest = (JoinRoomRequest)object;
                    try {
                        gameRoom.addPlayer(connection.getID(), joinRoomRequest.playerName);
                        RoomJoinedResponse roomJoinedResponse = new RoomJoinedResponse(gameRoom.currentPlayers - 1);
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

        new Timer().schedule(new TimerTask() {
                                 @Override
                                 public void run() {
                                     updatePlayerCount();
                                     sendListOfNames();
                                 }
                             }
                , 0, CHECK_CONNECTION_RATE);
        
    }

    @Override
    protected void send(Object object, int roomID) {
        if(gameRoom != null) {
            for(NamePair connectionID: gameRoom.connectionSet) {
                if(connectionID.getKey() != -1)
                    sendToTCP(connectionID.getKey(), object);
                else if(object instanceof GameResponse)
                    gameOwner.getUpdates((GameResponse)object);
                else
                    gameOwner.getRewards((RewardResponse)object);
            }
        }
    }


    private void updatePlayerCount() {
            gameRoom.currentPlayers = 1;
            Connection[] connections = getConnections();
            for(Connection connection: connections) {
                if(connection.isConnected() && connection.getID() != -1)
                    ++gameRoom.currentPlayers;
            }
    }


    private void sendListOfNames() {
        if(!gameRoom.isRunning) {
            NameListResponse nameListResponse = new NameListResponse();
            for(NamePair namePair : gameRoom.connectionSet) {
                nameListResponse.arrayList.add(namePair.getValue());
            }
            sendObjectToAllButHost(nameListResponse);
            gameOwner.observer.updateWaitingRoom(nameListResponse);
        }
    }

    public void startGame() {
        gameRoom.isRunning = true;
        sendObjectToAllButHost(new StartGameResponse());
    }


    public void closeGame() {
        sendObjectToAllButHost(new RoomClosedResponse());
        try {
            gameRoom.removePlayer(-1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        close();
        stop();
    }

    private void sendObjectToAllButHost(Object object) {
        for(NamePair namePair: gameRoom.connectionSet) {
            if(namePair.getKey() != -1)
                sendToTCP(namePair.getKey(), object);
        }
    }
}


package com.main.Networking;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.main.GameManager;
import com.main.Networking.requests.GameRequest;
import com.main.Networking.requests.GetRoomInfoRequest;
import com.main.Networking.requests.JoinRoomRequest;
import com.main.Networking.requests.LeaveRoomRequest;
import com.main.Networking.responses.*;
import com.main.SuperManager;
import com.main.TestController;

import java.io.IOException;
import java.net.BindException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The LocalServer class allows user to host a server accessible on his LAN. The local server uses a port
 * different to the one used by the main server.
 * @see GameServer
 * @author Piotr Satała
 */
public class LocalServer extends GameServer {

    /**
     * Rate at which local server checks connection status
     */
    private static final int CHECK_CONNECTION_RATE = 1000;


    private final GameRoom gameRoom;
    public SuperManager superManager;
    private final GameManager gameOwner;



    /**
     * Public constructor for LocalServer class
     * @param tcpSecondPortNumber tcp port for local server
     * @param udpSecondPortNumber udp port for local server
     * @param hostName name of user who is hosting the local server
     * @param maxPlayers max number of players on server
     * @throws IOException thrown when local server is unable to start
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

        //create new timer which updates count of players connected and sends them a list of names of connected players
        if(!TestController.isJUnitTest()) {
            new Timer().schedule(new TimerTask() {
                                     @Override
                                     public void run() {
                                         updatePlayerCount();
                                         sendListOfNames();
                                     }
                                 }
                    , 0, CHECK_CONNECTION_RATE);
        }

    }


    /**
     * Method responsible for sending game updates and rewards to clients
     * @param object object to be sent to clients
     * @param roomID always -1 in case of local server
     */
    @Override
    protected void send(Object object, int roomID) {
        if(gameRoom != null) {
            for(NamePair connectionID: gameRoom.connectionSet) {
                if(connectionID.getKey() != -1) //client is not host
                    sendToTCP(connectionID.getKey(), object);
                else if(object instanceof GameResponse) //client is host and object is general game updates
                    gameOwner.getUpdates((GameResponse)object);
                else //client is host and object is rewards
                    gameOwner.getRewards((RewardResponse)object);
            }
        }
    }



    /**
     * Method checks which players are still connected to the local server
     * and updates number of current players. Host is always connected.
     */
    private void updatePlayerCount() {
            gameRoom.currentPlayers = 1; //add host
            Connection[] connections = getConnections();
            for(Connection connection: connections) {
                if(connection.isConnected() && connection.getID() != -1) //for each connection that is not a host
                    ++gameRoom.currentPlayers;
            }
    }



    /**
     * Method sends list of names of all connected players to those players.
     * This only applies when game is in the waiting room phase.
     */
    private void sendListOfNames() {
        if(!gameRoom.isRunning) { //if in waiting room
            NameListResponse nameListResponse = new NameListResponse();
            for(NamePair namePair : gameRoom.connectionSet) {
                nameListResponse.arrayList.add(namePair.getValue());
            }
            sendObjectToAllButHost(nameListResponse);
            gameOwner.observer.updateWaitingRoom(nameListResponse); //inform host
        }
    }



    /**
     * Method called when host clicks the "Start Game" button. It informs all other
     * clients that the game has started.
     */
    public void startGame() {
        gameRoom.isRunning = true;
        sendObjectToAllButHost(new StartGameResponse());
    }


    /**
     * Method called when host exits the game. His lack of presence means the game needs to be
     * terminated. This information is sent to all other connected players.
     */
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


    /**
     * Method responsible for sending an object to all players connected to the server except the host.
     * This is useful due to the ubiquity of such need (host can get the necessary information through a function
     * call, all others need networking to get that information)
     * @param object object to be sent to players
     */
    private void sendObjectToAllButHost(Object object) {
        for(NamePair namePair: gameRoom.connectionSet) { //for each connection
            if(namePair.getKey() != -1)                  //that is not a host
                sendToTCP(namePair.getKey(), object);
        }
    }
}

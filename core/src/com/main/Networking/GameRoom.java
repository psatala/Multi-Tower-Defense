
package com.main.Networking;


import java.net.InetAddress;
import java.util.HashSet;

/**
 * The GameRoom class simulates a room on the network level in which a game can be played.
 * It consists of IDs of clients connections to the room and other room metadata.
 * @author Piotr Satała
 */
public class GameRoom {

    //static variables
    public static final int LOCAL = 0;
    public static final int GLOBAL = 1;
    private static int nextRoomID = 0;

    //public members
    public int roomID;
    public String hostName;
    public int currentPlayers;
    public int maxPlayers;
    public int gameType;
    public HashSet<Integer> connectionSet;
    public InetAddress ipOfHost; //ip for client to determine room host


    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameRoom() {
        roomID = -1;
        hostName = "";
        currentPlayers = 0;
        maxPlayers = 1;
        gameType = GLOBAL;
        connectionSet = new HashSet<>();
    }


    /**
     * Public constructor for GameRoom class
     * @param hostName name of either the client to request creation of the room from main server or the local server
     * @param maxPlayers max number of players in a game
     * @param gameType type of game - either GLOBAL - managed by main server or LOCAL - managed by a local server
     * @param connectionID either ID of the connection between the client who requested this game and the main
     * server or -1 if the game is LOCAL
     */
    public GameRoom(String hostName, int maxPlayers, int gameType, int connectionID) {

        //set init
        connectionSet = new HashSet<>();
        
        //copy parameters
        this.hostName = hostName;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;

        //only one player at the beginning
        currentPlayers = 1;
        connectionSet.add(connectionID);
        
        //set room id
        roomID = nextRoomID;
        ++nextRoomID;

    }


    /**
     * Add player to this room
     * @param connectionID ID of connection between client and server (main/local)
     * @throws Exception room already full
     */
    public void addPlayer(int connectionID) throws Exception {
        if(currentPlayers == maxPlayers)
            throw new Exception("Game already full!");
        else{
            ++currentPlayers;
            connectionSet.add(connectionID);
        }
    }


    /**
     * Remove player from this room
     * @param connectionID ID of connection between client and server (main/local)
     * @throws Exception room is now empty
     */
    public void removePlayer(int connectionID) throws Exception {
        --currentPlayers;
        connectionSet.remove(connectionID);
        if(0 == currentPlayers)
            throw new Exception("This room has been closed");
    }


    /**
     * Print information about the room
     */
    public String printRoomInfo() {
        String infoString = "Room id: " + roomID + " IP address: " + ipOfHost + " host name: " + hostName + " players: " + currentPlayers + "/" + maxPlayers + " game type: ";

        if(gameType == GLOBAL)
            infoString = infoString + "Global";
        else
            infoString = infoString + "Local";
        
        return infoString;
    }

    public static int getLastRoomID() {
        return nextRoomID - 1;
    }
}
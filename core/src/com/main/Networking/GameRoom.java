
package com.main.Networking;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;

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
    public Integer roomID;
    public String hostName;
    public Integer currentPlayers;
    public Integer maxPlayers;
    public Integer gameType;
    public HashSet<NamePair> connectionSet;
    public InetAddress ipOfHost; //ip for client to determine room host
    public String macAddress;
    public boolean isRunning;

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
        isRunning = false;
    }


    /**
     * Public constructor for GameRoom class
     * @param hostName name of either the client to request creation of the room from main server or the local server
     * @param maxPlayers max number of players in a game
     * @param gameType type of game - either GLOBAL - managed by main server or LOCAL - managed by a local server
     * @param connectionID either ID of the connection between the client who requested this game and the main
     * server or -1 if the game is LOCAL
     * @param name name of game creator
     */
    public GameRoom(String hostName, int maxPlayers, int gameType, int connectionID, String name) {

        //set init
        connectionSet = new HashSet<>();

        //copy parameters
        this.hostName = hostName;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;

        //only one player at the beginning
        currentPlayers = 1;
        connectionSet.add(new NamePair(connectionID, name));
        
        //set room id
        roomID = nextRoomID;
        ++nextRoomID;

        //game starts in the waiting room
        isRunning = false;

        try {
            macAddress = "null"; //make sure MAC address is null at first
            Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
            while(networks.hasMoreElements() && macAddress.equals("null")) {
                NetworkInterface network = networks.nextElement();
                byte[] mac = network.getHardwareAddress();
                macAddress = Arrays.toString(mac);
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        
    }


    /**
     * Add player to this room
     * @param connectionID ID of connection between client and server (main/local)
     * @throws Exception room already full
     */
    public void addPlayer(int connectionID, String name) throws Exception {
        if(currentPlayers == maxPlayers)
            throw new Exception("Game already full!");
        else{
            ++currentPlayers;
            connectionSet.add(new NamePair(connectionID, name));
        }
    }


    /**
     * Remove player from this room
     * @param connectionID ID of connection between client and server (main/local)
     * @throws Exception room is now empty
     */
    public void removePlayer(int connectionID) throws Exception {
        --currentPlayers;
        for(NamePair namePair: connectionSet)
            if(namePair.getKey() == connectionID)
                connectionSet.remove(namePair);
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


    /**
     * Get general information about the room so the user can see it
     * @return vector of strings containing info about the room
     */
    public Vector<String> getRoomInfo() {

        Vector<String> infoVector = new Vector<>();
        infoVector.add(roomID.toString());
        if(ipOfHost != null)
            infoVector.add(ipOfHost.toString());
        else
            infoVector.add("Unknown");
        infoVector.add(hostName);
        infoVector.add(currentPlayers.toString() + "/" + maxPlayers.toString());
        if(gameType == GLOBAL)
            infoVector.add("Global");
        else
            infoVector.add("Local");

        return infoVector;
    }


    /**
     * Get ID of the last created room
     * @return ID of the last created room
     */
    public static int getLastRoomID() {
        return nextRoomID - 1;
    }
}
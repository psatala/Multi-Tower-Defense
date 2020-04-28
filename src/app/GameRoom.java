package app;

import java.util.HashSet;

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

    public GameRoom() {
        roomID = -1;
        hostName = "";
        currentPlayers = 0;
        maxPlayers = 1;
        gameType = GLOBAL;
        connectionSet = new HashSet<>();
    }

    public GameRoom(String hostName, int maxPlayers, int gameType, int connectionID) {

        //set init
        connectionSet = new HashSet<>();
        
        //copy
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

    public void addPlayer(int connectionID) throws Exception {
        if(currentPlayers == maxPlayers)
            throw new Exception("Game already full!");
        else{
            ++currentPlayers;
            connectionSet.add(connectionID);
        }
    }

    public void removePlayer(int connectionID) throws Exception {
        --currentPlayers;
        connectionSet.remove(connectionID);
        if(0 == currentPlayers)
            throw new Exception("This room has been closed");
    }

    public void printRoomInfo() {
        System.out.print("Room id: " + roomID + " host name: " + hostName + " players: " + currentPlayers + "/" + maxPlayers + " game type: ");
        if(gameType == GLOBAL)
            System.out.println("Global");
        else
            System.out.println("Local");
    }
}
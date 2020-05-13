
package com.main.Networking.requests;

import com.main.Networking.GameRoom;

/**
 * The CreateRoomRequest class is a request from client to main server that he would like to open
 * a room managed by the main server.
 * @author Piotr Satała
 */
public class CreateRoomRequest {
    public String hostName;
    public int maxPlayers;
    public int gameType;


    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public CreateRoomRequest() {
        hostName = "";
        maxPlayers = 1;
        gameType = GameRoom.GLOBAL;
    }

    /**
     * Public constructor for CreateRoomRequest class
     * @param hostName name of player sendind the request
     * @param maxPlayers max number of players in this room
     * @param gameType type of game, here mostly global
     */
    public CreateRoomRequest(String hostName, int maxPlayers, int gameType) {
        this.hostName = hostName;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
    }
}
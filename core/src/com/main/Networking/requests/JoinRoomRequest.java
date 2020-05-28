
package com.main.Networking.requests;

/**
 * The JoinRoomRequest class is request from client to server to join a given room
 * that he is hosting.
 * @author Piotr Satała
 */
public class JoinRoomRequest {
    
    public int roomID;
    public String playerName;

    /**
     * Public empty constructor used mostly for joining local servers which can only host one room
     */
    public JoinRoomRequest() {
        roomID = -1;
        playerName = "";
    }


    /**
     * Public constructor for JoinRoomRequest class
     * @param playerName name of joining player
     */
    public JoinRoomRequest(String playerName) {
        this.playerName = playerName;
    }


    /**
     * Public constructor for JoinRoomRequest class
     * @param roomID ID of room client wants to join
     * @param playerName name of joining player
     */
    public JoinRoomRequest(int roomID, String playerName) {
        this.roomID = roomID;
        this.playerName = playerName;
    }
}
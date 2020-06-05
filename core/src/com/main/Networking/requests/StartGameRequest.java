package com.main.Networking.requests;

/**
 * The StartGameRequest class is a request from client to server to start the game
 * the player has created / hosted.
 * @author Piotr Satała
 */
public class StartGameRequest {
    private int roomID;


    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public StartGameRequest() {
        roomID = -1;
    }


    /**
     * Public constructor for StartGameRequest class
     * @param roomID id of room (game) to be started
     */
    public StartGameRequest(int roomID) {
        this.roomID = roomID;
    }


    /**
     * Getter for id of room
     * @return id of room
     */
    public int getRoomID() {
        return roomID;
    }


    /**
     * Setter for id of room
     * @param roomID new id of room
     */
    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }
}

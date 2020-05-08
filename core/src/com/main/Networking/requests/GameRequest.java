
package com.main.Networking.requests;

/**
 * The GameRequest class is a request transferring core game data from client to the server.
 * @author Piotr Satała
 */
public class GameRequest {
    private String message;
    private int roomID;

    
    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameRequest() {
        setMessage("");
        setRoomID(-1);
    }


    /**
     * Public constructor for GameRequest class
     * @param roomID ID of room the client is in
     */
    public GameRequest(int roomID) {
        setRoomID(roomID);
    }


    /**
     * Public constructor for GameRequest class
     * @param initialMessage message to be sent to the server
     */
    public GameRequest(String initialMessage) {
        setMessage(initialMessage);
    }




    //----------------------------------getters---------------------------------------------//



    /**
     * Getter for message to server
     * @return message to server
     */
    public String getMessage() {
        return message;
    }


    /**
     * Getter for ID of room client is in
     * @return ID of room client is in
     */
    public int getRoomID() {
        return roomID;
    }


    //----------------------------------setters--------------------------------------------//


    /**
     * Setter for message to server
     * @param newMessage message to server
     */
    public void setMessage(String newMessage) {
        message = newMessage;
    }


    /**
     * Setter for ID of room client is in
     * @param newRoomID ID of room client is in
     */
    public void setRoomID(int newRoomID) {
        roomID = newRoomID;
    }
}
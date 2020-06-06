
package com.main.Networking.requests;

import java.util.Vector;

/**
 * The GameRequest class is a request transferring core game data from client to the server.
 * @author Piotr Satała
 */
public class GameRequest {
    private Vector<String> message;
    private int roomID;

    
    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameRequest() {
        setMessage(new Vector<String>());
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
    public GameRequest(Vector<String> initialMessage) {
        setMessage(initialMessage);
    }




    //----------------------------------getters---------------------------------------------//



    /**
     * Getter for message to server
     * @return message to server
     */
    public Vector<String> getMessage() {
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
    public void setMessage(Vector<String> newMessage) {
        message = newMessage;
    }


    /**
     * Setter for ID of room client is in
     * @param newRoomID ID of room client is in
     */
    public void setRoomID(int newRoomID) {
        roomID = newRoomID;
    }


    //------------------------------------other---------------------------------------------//


    /**
     * Method appends one request
     * @param newMessage new request
     */
    public void appendMessage(String newMessage) {
        message.add(newMessage);
    }


    /**
     * Method clears the message vector
     */
    public void clearMessage() {
        message.clear();
    }

}
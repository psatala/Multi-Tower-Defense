
package com.main.Networking.responses;

import java.util.Vector;

/**
 * The GameResponse class is a response from server to client with core game data.
 * @author Piotr Satała
 */
public class GameResponse {

    private Vector<String> message;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameResponse() {
        setMessage(new Vector<String>());
    }

    /**
     * Public constructor for GameResponse class
     * @param initialMessage message to be send to client
     */
    public GameResponse(Vector<String> initialMessage) {
        setMessage(initialMessage);
    }


    /**
     * Getter for message to client
     * @return message to client
     */
    public Vector<String> getMessage() {
        return message;
    }


    /**
     * Setter for message to client
     * @param newMessage message to client
     */
    public void setMessage(Vector<String> newMessage) {
        message = newMessage;
    }


    /**
     * Method appends one response
     * @param newMessage new response
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
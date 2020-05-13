
package com.main.Networking.responses;

import java.util.Vector;

/**
 * The RewardResponse class is a response from server to client with game data relating rewards.
 * @author Piotr Satała
 */
public class RewardResponse {

    private Vector<String> message;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public RewardResponse() {
        setMessage(new Vector<String>());
    }

    /**
     * Public constructor for RewardResponse class
     * @param initialMessage message to be send to client
     */
    public RewardResponse(Vector<String> initialMessage) {
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
     * Function appends one response
     * @param newMessage new response
     */
    public void appendMessage(String newMessage) {
        message.add(newMessage);
    }


    /**
     * Function clears the message vector
     */
    public void clearMessage() {
        message.clear();
    }

}
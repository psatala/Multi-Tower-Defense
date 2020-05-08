
package com.main.Networking.responses;

/**
 * The GameResponse class is a response from server to client with core game data.
 * @author Piotr Satała
 */
public class GameResponse {

    private String message;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public GameResponse() {
        setMessage("");
    }

    /**
     * Public constructor for GameResponse class
     * @param initialMessage message to be send to client
     */
    public GameResponse(String initialMessage) {
        setMessage(initialMessage);
    }


    /**
     * Getter for message to client
     * @return message to client
     */
    public String getMessage() {
        return message;
    }


    /**
     * Setter for message to client
     * @param newMessage message to client
     */
    public void setMessage(String newMessage) {
        message = newMessage;
    }
}

package com.main.Networking.responses;

/**
 * The Control Response class is a response from server to a client informing him
 * about some error that happened during processing his request.
 * @author Piotr Satała
 */
public class ControlResponse {
    
    private String message;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public ControlResponse() {
        setMessage("");
    }


    /**
     * Public constructor for ControlResponse class
     * @param initialMessage control message to be transported
     */
    public ControlResponse(String initialMessage) {
        setMessage(initialMessage);
    }


    /**
     * Getter for control message
     * @return control message
     */
    public String getMessage() {
        return message;
    }


    /**
     * Setter for control message
     * @param newMessage new control message
     */
    public void setMessage(String newMessage) {
        message = newMessage;
    }
}
package com.main.Networking;


/**
 * The UpdatesListener interface is used to inform the networking side of the application
 * that the gameplay side wants to send updates to the other endpoint of the connection.
 * @author Piotr Satała
 */
public interface UpdatesListener {

    /**
     * Method called when gameplay side wants to send some object
     * @param object object to be sent
     * @param roomID id of the room the data in the object refers to
     */
    void updatesPending(Object object, int roomID);
}

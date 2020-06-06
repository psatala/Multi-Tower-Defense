package com.main.Networking;

import com.esotericsoftware.kryonet.Server;

/**
 * The GameServer class is an abstract class which provides functionality common across
 * main and local servers.
 * @see MainServer
 * @see LocalServer
 * @author Piotr Satała
 */
public abstract class GameServer extends Server {
    public UpdatesListener updatesListener;

    /**
     * Public empty constructor for GameServer class
     */
    public GameServer() {
        super();
        updatesListener = new UpdatesListener() { //add listener for updates from gameplay manager
            @Override
            public void updatesPending(Object object, int roomID) {
                send(object, roomID);
            }
        };
    }


    /**
     * Protected abstract method responsible for sending data to connected clients.
     * Main and local servers handle this differently, therefore they are obliged to
     * override this method
     * @param object object to be sent to clients
     * @param roomID id of the room the data refers to, always -1 in case of local server
     */
    protected abstract void send(Object object, int roomID);
}

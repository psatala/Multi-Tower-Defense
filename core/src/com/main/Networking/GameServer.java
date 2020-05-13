package com.main.Networking;

import com.esotericsoftware.kryonet.Server;

public abstract class GameServer extends Server {
    public UpdatesListener updatesListener;

    public GameServer() {
        super();
        updatesListener = new UpdatesListener() {
            @Override
            public void updatesPending(Object object, int roomID) {
                send(object, roomID);
            }
        };
    }

    protected abstract void send(Object object, int roomID);
}

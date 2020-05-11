package com.main.Networking;


public interface UpdatesListener {
    void updatesReceived(Object object);
    void updatesPending(Object object, int roomID);
}

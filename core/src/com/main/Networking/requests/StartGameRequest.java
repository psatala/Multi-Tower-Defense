package com.main.Networking.requests;

public class StartGameRequest {
    int roomID;

    public StartGameRequest() {
        roomID = -1;
    }

    public StartGameRequest(int roomID) {
        this.roomID = roomID;
    }
}

package com.main.Networking.requests;

public class StartGameRequest {
    private int roomID;

    public StartGameRequest() {
        roomID = -1;
    }

    public StartGameRequest(int roomID) {
        this.roomID = roomID;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }
}

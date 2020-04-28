package app.requests;

import app.GameRoom;

public class CreateRoomRequest {
    public String hostName;
    public int maxPlayers;
    public int gameType;

    public CreateRoomRequest() {
        hostName = "";
        maxPlayers = 1;
        gameType = GameRoom.GLOBAL;
    }

    public CreateRoomRequest(String hostName, int maxPlayers, int gameType) {
        this.hostName = hostName;
        this.maxPlayers = maxPlayers;
        this.gameType = gameType;
    }
}
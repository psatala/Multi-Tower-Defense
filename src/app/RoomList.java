package app;

import java.util.HashMap;

public class RoomList {
    private HashMap<Integer, GameRoom> list;

    public RoomList() {
        list = new HashMap<>();
    }

    public void add(GameRoom gameRoom) {
        list.put(gameRoom.roomID, gameRoom);
    }

    public GameRoom get(int roomID) {
        return list.get(roomID);
    }

    public void print() {
        for(int roomID: list.keySet())
            list.get(roomID).printRoomInfo();
    }
}
package app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class RoomList {
    private HashMap<Integer, GameRoom> list;

    public RoomList() {
        list = new HashMap<>();
    }

    public void add(GameRoom gameRoom) {
        list.put(gameRoom.roomID, gameRoom);
    }

    public void putALL(RoomList anotherRoomList) {
        list.putAll(anotherRoomList.list);
    }

    public GameRoom get(int roomID) {
        return list.get(roomID);
    }

    public void remove(int roomID) {
        list.remove(roomID);
    }

    public void print() {
        int i = 0;
        for(int roomID: list.keySet()) {
            System.out.print(i + ": ");
            list.get(roomID).printRoomInfo();
            ++i;
        }
            
    }

    public ArrayList<Integer> getArrayOfKeys() {
        return new ArrayList<Integer>(list.keySet());
    }

    public boolean containsKey(Integer key) {
        return list.containsKey(key);
    }

    public int getMaxKey() {
        return Collections.max(list.keySet());
    }

    public void clear() {
        list.clear();
    }
}
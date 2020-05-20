
package com.main.Networking;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * The RoomList class is responsible for handling a list of rooms stored by the main server, but also for
 * receiving it from main server and all locals server on the client side. The class wraps
 * HashMap<Integer, GameRoom> in order to provide additional utility for the program.
 * @author Piotr Satała
 */
public class RoomList {
    private final HashMap<Integer, GameRoom> list;

    /**
     * Public constructor for RoomList class
     */
    public RoomList() {
        list = new HashMap<>();
    }


    /**
     * Add one room to a list of rooms
     * @param gameRoom room to add
     */
    public void add(GameRoom gameRoom) {
        list.put(gameRoom.roomID, gameRoom);
    }


    /**
     * Add all rooms from a different room list to this room list
     * @param anotherRoomList different room list
     */
    public void putALL(RoomList anotherRoomList) {
        list.putAll(anotherRoomList.list);
    }


    /**
     * Get value for a given key - here roomID
     * @param roomID id of the room (key for the hashmap)
     * @return game room of a given id
     */
    public GameRoom get(int roomID) {
        return list.get(roomID);
    }


    /**
     * Remove given room from list
     * @param roomID id of room to remove
     */
    public void remove(int roomID) {
        list.remove(roomID);
    }


    /**
     * Print the entire list of rooms in a suitable format
     */
    public void print() {
        int i = 0;
        for(int roomID: list.keySet()) {
            System.out.print(i + ": ");
            System.out.println(list.get(roomID).printRoomInfo());
            ++i;
        }
            
    }


    /**
     * Get all keys stored in the HashMap in the form of an ArrayList
     * @return ArrayList with all keys currently present in the HashMap
     */
    public ArrayList<Integer> getArrayOfKeys() {
        return new ArrayList<>(list.keySet());
    }


    /**
     * Check whether the list contains a room with a given key
     * @param key key to check for
     * @return true if key was found, false otherwise
     */
    public boolean containsKey(Integer key) {
        return list.containsKey(key);
    }


    /**
     * Get key with the greatest value currently present in the HashMap
     * @return key with the greatest value
     */
    public int getMaxKey() {
        return Collections.max(list.keySet());
    }


    /**
     * Clear the contents of the HashMap
     */
    public void clear() {
        list.clear();
    }
}
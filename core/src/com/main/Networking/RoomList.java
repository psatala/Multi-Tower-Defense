
package com.main.Networking;


import java.util.*;

/**
 * The RoomList class is responsible for handling a list of rooms stored by the main server, but also for
 * receiving it from main server and all locals server on the client side. The class wraps
 * HashMap{@literal <}Integer, GameRoom{@literal >} in order to provide additional utility for the program.
 * @author Piotr Satała
 */
public class RoomList {
    private final HashMap<Integer, GameRoom> roomHashMap;

    /**
     * Public constructor for RoomList class
     */
    public RoomList() {
        roomHashMap = new HashMap<>();
    }


    /**
     * Add one room to a list of rooms
     * @param gameRoom room to add
     */
    public void add(GameRoom gameRoom) {
        roomHashMap.put(gameRoom.roomID, gameRoom);
    }


    /**
     * Add all rooms from a different room list to this room list
     * @param anotherRoomList different room list
     */
    public void putALL(RoomList anotherRoomList) {
        roomHashMap.putAll(anotherRoomList.roomHashMap);
    }


    /**
     * Get value for a given key - here roomID
     * @param roomID id of the room (key for the hashmap)
     * @return game room of a given id
     */
    public GameRoom get(int roomID) {
        return roomHashMap.get(roomID);
    }


    /**
     * Remove given room from list
     * @param roomID id of room to remove
     */
    public void remove(int roomID) {
        roomHashMap.remove(roomID);
    }


    /**
     * Print the entire list of rooms in a suitable format
     */
    public void print() {
        int i = 0;
        for(int roomID: roomHashMap.keySet()) {
            System.out.print(i + ": ");
            System.out.println(roomHashMap.get(roomID).printRoomInfo());
            ++i;
        }
            
    }


    /**
     * Get all keys stored in the HashMap in the form of an ArrayList
     * @return ArrayList with all keys currently present in the HashMap
     */
    public ArrayList<Integer> getArrayOfKeys() {
        return new ArrayList<>(roomHashMap.keySet());
    }


    /**
     * Check whether the list contains a room with a given key
     * @param key key to check for
     * @return true if key was found, false otherwise
     */
    public boolean containsKey(Integer key) {
        return roomHashMap.containsKey(key);
    }


    /**
     * Get key with the greatest value currently present in the HashMap
     * @return key with the greatest value
     */
    public int getMaxKey() {
        return Collections.max(roomHashMap.keySet());
    }


    /**
     * Clear the contents of the HashMap
     */
    public void clear() {
        roomHashMap.clear();
    }


    /**
     * Filter out full rooms from the list of rooms
     * @return hash map with only non full rooms
     */
    public RoomList filterRoomList() {
        RoomList roomListToReturn = new RoomList();
        for(Map.Entry<Integer, GameRoom> entry: roomHashMap.entrySet()) {
            if(entry.getValue().currentPlayers != entry.getValue().maxPlayers)
                roomListToReturn.add(entry.getValue());
        }
        return roomListToReturn;
    }
}
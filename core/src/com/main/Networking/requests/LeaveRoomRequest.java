
package com.main.Networking.requests;

/**
 * The LeaveRoomRequest class is a request from client to server to leave a given room
 * that he is hosting.
 * @author Piotr Satała
 */
public class LeaveRoomRequest {
    
    public int roomID;

    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public LeaveRoomRequest() {
        roomID = -1;
    }


    /**
     * Public constructor for LeaveRoomRequest class
     * @param roomID ID of room client wants to leave
     */
    public LeaveRoomRequest(int roomID) {
        this.roomID = roomID;
    }
}
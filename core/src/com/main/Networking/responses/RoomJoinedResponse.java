
package com.main.Networking.responses;

/**
 * The RoomJoinedResponse class is a response from server to client informing him that he has joined the room
 * that he had been requesting to join. Informs the client about his ID within the room.
 * @author Piotr Satała
 */
public class RoomJoinedResponse {

    private int idWithinRoom = -1;

    /**
     * Public empty constructor
     */
    public RoomJoinedResponse() {}


    /**
     * Public constructor for RoomJoinedResponse
     * @param idWithinRoom id of the client within joined room
     */
    public RoomJoinedResponse(int idWithinRoom) {
        this.idWithinRoom = idWithinRoom;
    }


    /**
     * Getter for ID within the room
     * @return ID within the room
     */
    public int getIdWithinRoom() {
        return idWithinRoom;
    }


    /**
     * Setter for ID within the room
     * @param idWithinRoom new ID within the room
     */
    public void setIdWithinRoom(int idWithinRoom) {
        this.idWithinRoom = idWithinRoom;
    }
}
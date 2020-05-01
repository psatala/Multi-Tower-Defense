/**
 * The LeaveRoomRequest class is request from client to server to leave a given room
 * that he is hosting.
 * @author Piotr Satała
 */

package app.requests;

public class LeaveRoomRequest {
    
    public int roomID;

    /**
     * Public empty constructor used mostly for leaving local servers which can only host one room
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
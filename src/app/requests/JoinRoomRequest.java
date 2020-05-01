/**
 * The JoinRoomRequest class is request from client to server to join a given room
 * that he is hosting.
 * @author Piotr Satała
 */

package app.requests;

public class JoinRoomRequest {
    
    public int roomID;

    /**
     * Public empty constructor used mostly for joining local servers which can only host one room
     */
    public JoinRoomRequest() {
        roomID = -1;
    }


    /**
     * Public constructor for JoinRoomRequest class
     * @param roomID ID of room client wants to join
     */
    public JoinRoomRequest(int roomID) {
        this.roomID = roomID;
    }
}
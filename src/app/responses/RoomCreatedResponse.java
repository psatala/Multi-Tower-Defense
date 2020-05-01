/**
 * The RoomCreatedResponse class is a response from main server to client informing him that a room he
 * had been requesting has now been created and the client can start running the game.
 * @author Piotr Satała
 */

package app.responses;

public class RoomCreatedResponse {
    
    public int roomID;
    
    /**
     * Public empty constructor necessary for KryoNet to send instances of this class properly
     */
    public RoomCreatedResponse() {}


    /**
     * Public constructor for RoomCreatedResponse class
     * @param roomID ID of created room
     */
    public RoomCreatedResponse(int roomID) {
        this.roomID = roomID;
    }
}
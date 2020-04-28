package app.requests;

public class JoinRoomRequest {
    public int roomID;

    public JoinRoomRequest() {
        roomID = -1;
    }

    public JoinRoomRequest(int roomID) {
        this.roomID = roomID;
    }
}
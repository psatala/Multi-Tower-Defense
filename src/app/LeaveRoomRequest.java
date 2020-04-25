package app;

public class LeaveRoomRequest {
    public int roomID;

    public LeaveRoomRequest() {
        roomID = -1;
    }

    public LeaveRoomRequest(int roomID) {
        this.roomID = roomID;
    }
}
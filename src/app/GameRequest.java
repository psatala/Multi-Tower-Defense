package app;

public class GameRequest {
    private String message;
    private int roomID;

    public GameRequest() {
        setMessage("");
        setRoomID(-1);
    }

    public GameRequest(int roomID) {
        setRoomID(roomID);
    }

    public GameRequest(String initialMessage) {
        setMessage(initialMessage);
    }


    //getters

    public String getMessage() {
        return message;
    }

    public int getRoomID() {
        return roomID;
    }


    //setters

    public void setMessage(String newMessage) {
        message = newMessage;
    }

    public void setRoomID(int newRoomID) {
        roomID = newRoomID;
    }
}
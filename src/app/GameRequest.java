package app;

public class GameRequest {
    private String message;

    public GameRequest() {
        setMessage("");
    }

    public GameRequest(String initialMessage) {
        setMessage(initialMessage);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }
}
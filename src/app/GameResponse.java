package app;

public class GameResponse {
    private String message;

    public GameResponse() {
        setMessage("");
    }

    public GameResponse(String initialMessage) {
        setMessage(initialMessage);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }
}
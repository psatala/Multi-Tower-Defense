package app;

public class ControlResponse {
    private String message;

    public ControlResponse() {
        setMessage("");
    }

    public ControlResponse(String initialMessage) {
        setMessage(initialMessage);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String newMessage) {
        message = newMessage;
    }
}
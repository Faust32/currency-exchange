package model.response;

public class ErrorResponse {
    private int ID;
    private String message;

    public ErrorResponse(int ID, String message) {
        this.ID = ID;
        this.message = message;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

package service;

public class RequestHandler {

    public String trim(String requestURI) {
        String[] temp = requestURI.split("/");
        return temp[temp.length - 1].toUpperCase();
    }

    public boolean isRequestValid(String request) {
        return !request.matches("[A-Z]{3}") && !request.matches("[A-Z]{6}");
    }
}

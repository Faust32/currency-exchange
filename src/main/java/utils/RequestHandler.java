package utils;

public class RequestHandler {
    public String trim(String requestURI) {
        String[] temp = requestURI.split("/");
        return temp[temp.length - 1].toUpperCase();
    }
}

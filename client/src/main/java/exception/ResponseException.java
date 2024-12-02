package exception;

public class ResponseException extends Exception {

    public ResponseException(String message, int statusCode) {
        super(message);
    }

}

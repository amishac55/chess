package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
    final Integer statusCode;
    public DataAccessException(Integer errorCode, String message) {
        super(message);
        this.statusCode = errorCode;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getMessage() {
        return super.getMessage();
    }
}

package pt.quickLabPIV.exceptions;

public class InvalidOptionException extends UIException {


    /**
     * 
     */
    private static final long serialVersionUID = -8845367494488876396L;

    public InvalidOptionException(String _userMessage) {
        super(_userMessage);        
    }

    public InvalidOptionException(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public InvalidOptionException(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public InvalidOptionException(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public InvalidOptionException(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

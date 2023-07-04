package pt.quickLabPIV.exceptions;

public class InvalidProjectFileException extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = 7852082558485492881L;

    public InvalidProjectFileException(String _userMessage) {
        super(_userMessage);        
    }

    public InvalidProjectFileException(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public InvalidProjectFileException(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public InvalidProjectFileException(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public InvalidProjectFileException(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

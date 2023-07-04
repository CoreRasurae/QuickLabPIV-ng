package pt.quickLabPIV.exceptions;

public class InvalidExecutionEnvException extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = 8001632970005836499L;

    public InvalidExecutionEnvException(String _userMessage) {
        super(_userMessage);
    }

    public InvalidExecutionEnvException(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public InvalidExecutionEnvException(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public InvalidExecutionEnvException(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public InvalidExecutionEnvException(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

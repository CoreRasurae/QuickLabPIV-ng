package pt.quickLabPIV.exceptions;

public class UIException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 391223935088169813L;
    private String titleMessage;

    public UIException(String _userMessage) {
        titleMessage = _userMessage;
    }

    public UIException(String _userMessage, String message) {
        super(message);
        titleMessage = _userMessage;
    }

    public UIException(String _userMessage, Throwable cause) {
        super(cause);
        titleMessage = _userMessage;
    }

    public UIException(String _userMessage, String message, Throwable cause) {
        super(message, cause);
        titleMessage = _userMessage;
    }

    public UIException(String _userMessage, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        titleMessage = _userMessage;
    }
    
    public String getTitleMessage() {
        return titleMessage;
    }

}

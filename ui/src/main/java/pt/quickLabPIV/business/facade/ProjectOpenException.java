package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.exceptions.UIException;

public class ProjectOpenException extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = 3438957051123553389L;

    public ProjectOpenException(String _userMessage) {
        super(_userMessage);
    }

    public ProjectOpenException(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public ProjectOpenException(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public ProjectOpenException(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public ProjectOpenException(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

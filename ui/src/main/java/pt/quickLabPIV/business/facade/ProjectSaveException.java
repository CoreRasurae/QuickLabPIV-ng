package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.exceptions.UIException;

public class ProjectSaveException extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = 1255557950632113788L;

    public ProjectSaveException(String userMessage) {
        super(userMessage);
    }

    public ProjectSaveException(String userMessage, String message) {
        super(userMessage, message);
    }

    public ProjectSaveException(String userMessage, Throwable cause) {
        super(userMessage, cause);
    }

    public ProjectSaveException(String userMessage, String message, Throwable cause) {
        super(userMessage, message, cause);
    }

    public ProjectSaveException(String userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(userMessage, message, cause, enableSuppression, writableStackTrace);
    }
}

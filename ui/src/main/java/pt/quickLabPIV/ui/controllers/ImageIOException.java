package pt.quickLabPIV.ui.controllers;

import pt.quickLabPIV.exceptions.UIException;

public class ImageIOException extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = -7869510663444811984L;

    public ImageIOException(String _userMessage) {
        super(_userMessage);
    }

    public ImageIOException(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public ImageIOException(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public ImageIOException(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public ImageIOException(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

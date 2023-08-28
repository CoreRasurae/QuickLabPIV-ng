// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
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

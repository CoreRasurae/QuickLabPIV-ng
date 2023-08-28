// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.business.facade;

import pt.quickLabPIV.exceptions.UIException;

public class StrategyCreationFailed extends UIException {

    /**
     * 
     */
    private static final long serialVersionUID = 3007952325083213983L;

    public StrategyCreationFailed(String _userMessage) {
        super(_userMessage);
    }

    public StrategyCreationFailed(String _userMessage, String message) {
        super(_userMessage, message);
    }

    public StrategyCreationFailed(String _userMessage, Throwable cause) {
        super(_userMessage, cause);
    }

    public StrategyCreationFailed(String _userMessage, String message, Throwable cause) {
        super(_userMessage, message, cause);
    }

    public StrategyCreationFailed(String _userMessage, String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(_userMessage, message, cause, enableSuppression, writableStackTrace);
    }

}

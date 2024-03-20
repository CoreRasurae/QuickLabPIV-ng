// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas.validation;

public class VectorValidatorException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 225045805430365176L;

    public VectorValidatorException() {
    }

    public VectorValidatorException(String message) {
        super(message);
    }

    public VectorValidatorException(Throwable cause) {
        super(cause);
    }

    public VectorValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public VectorValidatorException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

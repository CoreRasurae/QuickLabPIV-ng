// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

public class UnknownImageFilteringException extends RuntimeException {

	/**
     * 
     */
    private static final long serialVersionUID = -8934701676291643082L;

    public UnknownImageFilteringException() {
	}

	public UnknownImageFilteringException(String message) {
		super(message);
	}

	public UnknownImageFilteringException(Throwable cause) {
		super(cause);
	}

	public UnknownImageFilteringException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownImageFilteringException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

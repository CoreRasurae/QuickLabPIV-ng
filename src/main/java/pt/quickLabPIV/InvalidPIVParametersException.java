// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

public class InvalidPIVParametersException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186872214247749685L;

	public InvalidPIVParametersException() {
	}

	public InvalidPIVParametersException(String message) {
		super(message);
	}

	public InvalidPIVParametersException(Throwable cause) {
		super(cause);
	}

	public InvalidPIVParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPIVParametersException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

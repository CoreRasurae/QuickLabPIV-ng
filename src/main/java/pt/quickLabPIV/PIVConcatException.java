// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

public class PIVConcatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186872214247749685L;

	public PIVConcatException() {
	}

	public PIVConcatException(String message) {
		super(message);
	}

	public PIVConcatException(Throwable cause) {
		super(cause);
	}

	public PIVConcatException(String message, Throwable cause) {
		super(message, cause);
	}

	public PIVConcatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

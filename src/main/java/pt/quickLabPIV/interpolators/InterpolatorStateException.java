// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.interpolators;

/**
 * This exception should be thrown when an Interpolator finds an inconsistent configuration, or otherwise,
 * runs into an inconsistency state, and as such both are considered relevant errors,
 * @author lpnm
 *
 */
public class InterpolatorStateException extends RuntimeException {

	

	/**
	 * 
	 */
	private static final long serialVersionUID = -6242740600268937272L;

	public InterpolatorStateException() {
	}

	public InterpolatorStateException(String message) {
		super(message);
	}

	public InterpolatorStateException(Throwable cause) {
		super(cause);
	}

	public InterpolatorStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterpolatorStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

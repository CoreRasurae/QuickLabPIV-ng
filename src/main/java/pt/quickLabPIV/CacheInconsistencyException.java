// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

public class CacheInconsistencyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5108650220050789672L;

	public CacheInconsistencyException() {
	}

	public CacheInconsistencyException(String message) {
		super(message);
	}

	public CacheInconsistencyException(Throwable cause) {
		super(cause);
	}

	public CacheInconsistencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheInconsistencyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

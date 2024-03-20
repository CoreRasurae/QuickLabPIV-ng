// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV;

public class UnsupportedImageFormat extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5063589978981618236L;

	public UnsupportedImageFormat() {
	}

	public UnsupportedImageFormat(String message) {
		super(message);
	}

	public UnsupportedImageFormat(Throwable cause) {
		super(cause);
	}

	public UnsupportedImageFormat(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedImageFormat(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

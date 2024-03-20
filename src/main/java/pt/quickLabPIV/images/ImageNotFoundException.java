// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

public class ImageNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5652663441533589736L;

	public ImageNotFoundException() {
	}

	public ImageNotFoundException(String message) {
		super(message);
	}

	public ImageNotFoundException(Throwable cause) {
		super(cause);
	}

	public ImageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

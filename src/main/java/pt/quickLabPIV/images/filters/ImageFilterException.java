// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images.filters;

public class ImageFilterException extends RuntimeException {


	/**
     * 
     */
    private static final long serialVersionUID = -5635112670887280880L;

	public ImageFilterException() {
	}

	public ImageFilterException(String message) {
		super(message);
	}

	public ImageFilterException(Throwable cause) {
		super(cause);
	}

	public ImageFilterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageFilterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

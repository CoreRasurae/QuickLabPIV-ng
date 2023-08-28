// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

public class ImageClippingException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2883395808923478579L;

	public ImageClippingException() {
	}

	public ImageClippingException(String message) {
		super(message);
	}

	public ImageClippingException(Throwable cause) {
		super(cause);
	}

	public ImageClippingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageClippingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

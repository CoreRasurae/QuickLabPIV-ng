// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV.images;

import pt.quickLabPIV.jobs.JobComputeException;

public class ImageReaderException extends JobComputeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3362841013524951681L;

	public ImageReaderException() {
	}

	public ImageReaderException(String message) {
		super(message);
	}

	public ImageReaderException(Throwable cause) {
		super(cause);
	}

	public ImageReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageReaderException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2020 Luís Mendes
 */
package pt.quickLabPIV;

public class MatrixConversionOverflow extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8976357921948519553L;

	public MatrixConversionOverflow() {
	}

	public MatrixConversionOverflow(String message) {
		super(message);
	}

	public MatrixConversionOverflow(Throwable cause) {
		super(cause);
	}

	public MatrixConversionOverflow(String message, Throwable cause) {
		super(message, cause);
	}

	public MatrixConversionOverflow(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

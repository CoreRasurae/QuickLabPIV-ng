// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

public class ExportFailedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7362570595603245886L;

	public ExportFailedException() {
	}

	public ExportFailedException(String message) {
		super(message);
	}

	public ExportFailedException(Throwable cause) {
		super(cause);
	}

	public ExportFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExportFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.exporter;

public class FieldAlreadyExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8941240530976316293L;

	public FieldAlreadyExistsException() {
	}

	public FieldAlreadyExistsException(String message) {
		super(message);
	}

	public FieldAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public FieldAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public FieldAlreadyExistsException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

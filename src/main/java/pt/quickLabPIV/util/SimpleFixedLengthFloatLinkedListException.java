// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.util;

public class SimpleFixedLengthFloatLinkedListException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1220083220171325860L;

	public SimpleFixedLengthFloatLinkedListException() {
	}

	public SimpleFixedLengthFloatLinkedListException(String description) {
		super(description);
	}

	public SimpleFixedLengthFloatLinkedListException(Throwable cause) {
		super(cause);
	}

	public SimpleFixedLengthFloatLinkedListException(String message, Throwable cause) {
		super(message, cause);
	}

	public SimpleFixedLengthFloatLinkedListException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

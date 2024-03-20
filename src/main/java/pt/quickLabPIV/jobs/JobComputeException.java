// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

public class JobComputeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3433159491391353183L;

	public JobComputeException() {
	}

	public JobComputeException(String message) {
		super(message);
	}

	public JobComputeException(Throwable cause) {
		super(cause);
	}

	public JobComputeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobComputeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

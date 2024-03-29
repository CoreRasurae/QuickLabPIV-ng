// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.jobs;

public class JobAnalyzeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8218351939641766198L;

	public JobAnalyzeException() {
	}

	public JobAnalyzeException(String message) {
		super(message);
	}

	public JobAnalyzeException(Throwable cause) {
		super(cause);
	}

	public JobAnalyzeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobAnalyzeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

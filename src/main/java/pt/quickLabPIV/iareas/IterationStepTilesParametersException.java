// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.iareas;

public class IterationStepTilesParametersException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6972512232036365808L;

	public IterationStepTilesParametersException() {
	}

	public IterationStepTilesParametersException(String message) {
		super(message);
	}

	public IterationStepTilesParametersException(Throwable cause) {
		super(cause);
	}

	public IterationStepTilesParametersException(String message, Throwable cause) {
		super(message, cause);
	}

	public IterationStepTilesParametersException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

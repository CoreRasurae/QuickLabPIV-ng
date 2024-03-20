// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

public class HistogramException extends RuntimeException {


	/**
     * 
     */
    private static final long serialVersionUID = -6480231540221327418L;

    public HistogramException() {
	}

	public HistogramException(String message) {
		super(message);
	}

	public HistogramException(Throwable cause) {
		super(cause);
	}

	public HistogramException(String message, Throwable cause) {
		super(message, cause);
	}

	public HistogramException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

// SPDX-License-Identifier: GPL-3.0-only
/*
 * QuickLabPIV-ng - A hybrid PIV and PIV software laboratory (new generation)
 *
 * Copyright (C) 2017 to present: Luís Mendes <luis.mendes@tecnico.ulisboa.pt>
 */
package pt.quickLabPIV.images;

public class InvalidWarpingModeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -953677658210757517L;

    public InvalidWarpingModeException() {
	}

	public InvalidWarpingModeException(String message) {
		super(message);
	}

	public InvalidWarpingModeException(Throwable cause) {
		super(cause);
	}

	public InvalidWarpingModeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidWarpingModeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

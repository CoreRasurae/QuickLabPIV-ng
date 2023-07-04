package pt.quickLabPIV.interpolators;

/**
 * This exception should be thrown when an Interpolator run into an individual issue, related to the input data
 * (Correlation matrix peak, correlation matrix data) or otherwise and as such, it could valid, to try a different
 * interpolator.
 * @author lpnm
 *
 */
public class InterpolatorFailedException extends RuntimeException {

	/**
     * 
     */
    private static final long serialVersionUID = -8020252400443578991L;

    public InterpolatorFailedException() {
	}

	public InterpolatorFailedException(String message) {
		super(message);
	}

	public InterpolatorFailedException(Throwable cause) {
		super(cause);
	}

	public InterpolatorFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterpolatorFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

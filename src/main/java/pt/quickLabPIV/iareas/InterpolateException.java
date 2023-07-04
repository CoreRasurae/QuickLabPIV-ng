package pt.quickLabPIV.iareas;

public class InterpolateException extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5798177397222435148L;

	public InterpolateException() {
	}

	public InterpolateException(String message) {
		super(message);
	}

	public InterpolateException(Throwable cause) {
		super(cause);
	}

	public InterpolateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterpolateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

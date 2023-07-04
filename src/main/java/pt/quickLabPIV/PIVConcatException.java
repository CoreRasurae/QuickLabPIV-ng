package pt.quickLabPIV;

public class PIVConcatException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186872214247749685L;

	public PIVConcatException() {
	}

	public PIVConcatException(String message) {
		super(message);
	}

	public PIVConcatException(Throwable cause) {
		super(cause);
	}

	public PIVConcatException(String message, Throwable cause) {
		super(message, cause);
	}

	public PIVConcatException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

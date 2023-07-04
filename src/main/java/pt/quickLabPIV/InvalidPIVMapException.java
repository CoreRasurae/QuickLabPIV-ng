package pt.quickLabPIV;

public class InvalidPIVMapException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9186872214247749685L;

	public InvalidPIVMapException() {
	}

	public InvalidPIVMapException(String message) {
		super(message);
	}

	public InvalidPIVMapException(Throwable cause) {
		super(cause);
	}

	public InvalidPIVMapException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidPIVMapException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

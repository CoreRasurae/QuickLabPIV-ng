package pt.quickLabPIV.exporter;

public class InvalidStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3859959449440627420L;

	public InvalidStateException() {
	}

	public InvalidStateException(String message) {
		super(message);
	}

	public InvalidStateException(Throwable cause) {
		super(cause);
	}

	public InvalidStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidStateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

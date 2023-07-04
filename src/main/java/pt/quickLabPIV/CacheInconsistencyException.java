package pt.quickLabPIV;

public class CacheInconsistencyException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5108650220050789672L;

	public CacheInconsistencyException() {
	}

	public CacheInconsistencyException(String message) {
		super(message);
	}

	public CacheInconsistencyException(Throwable cause) {
		super(cause);
	}

	public CacheInconsistencyException(String message, Throwable cause) {
		super(message, cause);
	}

	public CacheInconsistencyException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

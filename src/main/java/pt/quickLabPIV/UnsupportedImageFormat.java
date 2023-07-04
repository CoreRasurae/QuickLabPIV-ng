package pt.quickLabPIV;

public class UnsupportedImageFormat extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5063589978981618236L;

	public UnsupportedImageFormat() {
	}

	public UnsupportedImageFormat(String message) {
		super(message);
	}

	public UnsupportedImageFormat(Throwable cause) {
		super(cause);
	}

	public UnsupportedImageFormat(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedImageFormat(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
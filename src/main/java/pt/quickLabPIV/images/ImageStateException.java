package pt.quickLabPIV.images;

public class ImageStateException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5389905273394842682L;

	public ImageStateException() {
	}

	public ImageStateException(String message) {
		super(message);
	}

	public ImageStateException(Throwable cause) {
		super(cause);
	}

	public ImageStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

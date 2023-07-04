package pt.quickLabPIV.images;

public class ImageNotFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5652663441533589736L;

	public ImageNotFoundException() {
	}

	public ImageNotFoundException(String message) {
		super(message);
	}

	public ImageNotFoundException(Throwable cause) {
		super(cause);
	}

	public ImageNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageNotFoundException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

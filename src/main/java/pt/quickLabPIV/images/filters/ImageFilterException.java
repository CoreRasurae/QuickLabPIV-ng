package pt.quickLabPIV.images.filters;

public class ImageFilterException extends RuntimeException {


	/**
     * 
     */
    private static final long serialVersionUID = -5635112670887280880L;

	public ImageFilterException() {
	}

	public ImageFilterException(String message) {
		super(message);
	}

	public ImageFilterException(Throwable cause) {
		super(cause);
	}

	public ImageFilterException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageFilterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

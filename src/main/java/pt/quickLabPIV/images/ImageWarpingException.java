package pt.quickLabPIV.images;

public class ImageWarpingException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 5763695347296778255L;

    public ImageWarpingException() {
	}

	public ImageWarpingException(String message) {
		super(message);
	}

	public ImageWarpingException(Throwable cause) {
		super(cause);
	}

	public ImageWarpingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageWarpingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

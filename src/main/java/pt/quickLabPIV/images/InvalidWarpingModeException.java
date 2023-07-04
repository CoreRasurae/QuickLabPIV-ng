package pt.quickLabPIV.images;

public class InvalidWarpingModeException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -953677658210757517L;

    public InvalidWarpingModeException() {
	}

	public InvalidWarpingModeException(String message) {
		super(message);
	}

	public InvalidWarpingModeException(Throwable cause) {
		super(cause);
	}

	public InvalidWarpingModeException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidWarpingModeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

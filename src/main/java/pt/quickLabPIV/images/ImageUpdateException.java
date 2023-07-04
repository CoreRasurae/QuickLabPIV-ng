package pt.quickLabPIV.images;

import pt.quickLabPIV.jobs.JobComputeException;

public class ImageUpdateException extends JobComputeException {

	/**
     * 
     */
    private static final long serialVersionUID = 519468772339699537L;

    public ImageUpdateException() {
	}

	public ImageUpdateException(String message) {
		super(message);
	}

	public ImageUpdateException(Throwable cause) {
		super(cause);
	}

	public ImageUpdateException(String message, Throwable cause) {
		super(message, cause);
	}

	public ImageUpdateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

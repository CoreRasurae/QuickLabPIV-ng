package pt.quickLabPIV.jobs;

public class JobComputeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3433159491391353183L;

	public JobComputeException() {
	}

	public JobComputeException(String message) {
		super(message);
	}

	public JobComputeException(Throwable cause) {
		super(cause);
	}

	public JobComputeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobComputeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

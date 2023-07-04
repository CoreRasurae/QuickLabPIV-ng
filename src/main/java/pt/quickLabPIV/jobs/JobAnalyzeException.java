package pt.quickLabPIV.jobs;

public class JobAnalyzeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8218351939641766198L;

	public JobAnalyzeException() {
	}

	public JobAnalyzeException(String message) {
		super(message);
	}

	public JobAnalyzeException(Throwable cause) {
		super(cause);
	}

	public JobAnalyzeException(String message, Throwable cause) {
		super(message, cause);
	}

	public JobAnalyzeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

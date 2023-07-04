package pt.quickLabPIV;

public class MatrixDimensionOverflow extends RuntimeException {


	/**
	 * 
	 */
	private static final long serialVersionUID = -8179292217024038999L;

	public MatrixDimensionOverflow() {
	}

	public MatrixDimensionOverflow(String message) {
		super(message);
	}

	public MatrixDimensionOverflow(Throwable cause) {
		super(cause);
	}

	public MatrixDimensionOverflow(String message, Throwable cause) {
		super(message, cause);
	}

	public MatrixDimensionOverflow(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}

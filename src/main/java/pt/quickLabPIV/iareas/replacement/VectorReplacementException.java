package pt.quickLabPIV.iareas.replacement;

public class VectorReplacementException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 2242984815002796743L;

    public VectorReplacementException() {
    }

    public VectorReplacementException(String message) {
        super(message);
    }

    public VectorReplacementException(Throwable cause) {
        super(cause);
    }

    public VectorReplacementException(String message, Throwable cause) {
        super(message, cause);
    }

    public VectorReplacementException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

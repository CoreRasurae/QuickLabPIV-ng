package pt.quickLabPIV.maximum;

public class MaximumFinderException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 6308964316490387485L;

    public MaximumFinderException() {
    }

    public MaximumFinderException(String message) {
        super(message);
    }

    public MaximumFinderException(Throwable cause) {
        super(cause);
    }

    public MaximumFinderException(String message, Throwable cause) {
        super(message, cause);
    }

    public MaximumFinderException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

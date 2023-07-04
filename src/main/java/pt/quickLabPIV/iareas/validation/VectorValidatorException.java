package pt.quickLabPIV.iareas.validation;

public class VectorValidatorException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 225045805430365176L;

    public VectorValidatorException() {
    }

    public VectorValidatorException(String message) {
        super(message);
    }

    public VectorValidatorException(Throwable cause) {
        super(cause);
    }

    public VectorValidatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public VectorValidatorException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}

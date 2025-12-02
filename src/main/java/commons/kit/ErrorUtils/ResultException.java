package commons.kit.ErrorUtils;


/**
 * Exception thrown by Result.getOrThrow() when called on a Failure.
 *
 * <p>Wraps the original error to provide context about the Result failure.</p>
 */
public class ResultException extends RuntimeException {
    private final Object error;

    /**
     * Creates a ResultException wrapping an error object.
     */
    public ResultException(Object error) {
        super(String.valueOf(error));
        this.error = error;
    }

    /**
     * Creates a ResultException with a custom message.
     */
    public ResultException(String message) {
        super(message);
        this.error = message;
    }

    /**
     * Returns the wrapped error object.
     */
    public Object getError() {
        return error;
    }

    /**
     * Returns the error cast to the expected type.
     * Use with caution - may throw ClassCastException.
     */
    @SuppressWarnings("unchecked")
    public <T> T getErrorAs(Class<T> type) {
        return (T) error;
    }
}
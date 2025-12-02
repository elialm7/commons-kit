package commons.kit.ErrorUtils;

/**
 * A functional interface representing a function that takes an argument and returns a result.
 *
 * <p>Similar to {@link java.util.function.Function} but with a more concise name
 * for use in fluent APIs.</p>
 *
 * @param <T> the input type
 * @param <R> the result type
 */
@FunctionalInterface
public interface Fn<T, R> {
    /**
     * Applies this function to the given argument.
     *
     * @param t the input argument
     * @return the function result
     */
    R apply(T t);
}

package commons.kit.ErrorUtils;
/**
 * A supplier that may throw checked exceptions.
 *
 * <p>This interface is designed for use with Result.of() to safely wrap
 * operations that may fail.</p>
 *
 * @param <T> the type of results supplied by this supplier
 */
public interface CheckedSupplier<T> {
    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return a result
     * @throws Throwable if unable to supply a result
     */
    T get() throws Throwable;
}

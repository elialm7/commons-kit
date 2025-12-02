package commons.kit.ErrorUtils;


/**
 * Marker interface representing an empty/absent value.
 *
 * <p>Prefer using Empty over null in Result.ok() to make absence explicit.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>
 * Result&lt;String, Object&gt; result = Result.ok(Empty.INSTANCE);
 * </pre>
 */
 public interface Empty {

    /**
     * Singleton instance representing absence of a value.
     */
    Empty INSTANCE = new Empty() {
        @Override
        public String toString() {
            return "Empty";
        }
    };
}
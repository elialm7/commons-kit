package commons.kit.JsonUtils;


import commons.kit.ErrorUtils.Result;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Facade for JSON operations with pluggable implementations.
 *
 * <p>All methods return {@link Result} to force explicit error handling.
 * The default implementation uses Jackson, but can be swapped via SPI.</p>
 *
 * <p><strong>Features:</strong></p>
 * <ul>
 *   <li>Type Alchemy: Convert between any Java types through JSON (Map → POJO, POJO → Map)</li>
 *   <li>Safe Navigation: XPath-style paths like "users.0.address.city"</li>
 *   <li>Deep Merge: Combine JSON objects recursively</li>
 *   <li>Tree Pruning: Remove nulls, empty strings, empty arrays/objects</li>
 * </ul>
 *
 * @author commons-kit
 * @version 1.3.0-SNAPSHOT
 */
public final class JsonUtils {

    private static JsonProvider provider = new JacksonJsonProvider();

    // Private constructor to prevent instantiation
    private JsonUtils() {
        throw new AssertionError("No JsonUtils instances for you!");
    }

    /**
     * Sets the JSON provider implementation.
     *
     * <p>Allows switching from Jackson to Gson, Moshi, etc.</p>
     *
     * @param newProvider the provider to use
     */
    public static void setProvider(JsonProvider newProvider) {
        if (newProvider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }
        provider = newProvider;
    }

    // ========== Core Operations ==========

    /**
     * Serializes an object to JSON string.
     *
     * <p><strong>Configuration:</strong></p>
     * <ul>
     *   <li>Ignores null values</li>
     *   <li>Formats dates as ISO-8601</li>
     *   <li>Pretty-prints for readability</li>
     * </ul>
     *
     * @param value the object to serialize
     * @param <E> the error type (typically String or Throwable)
     * @return Result containing JSON string
     */
    public static <E> Result<E, String> toJson(Object value) {
        return provider.toJson(value);
    }

    /**
     * Deserializes JSON string to an object of the specified type.
     *
     * @param json the JSON string
     * @param clazz the target class
     * @param <E> the error type
     * @param <T> the target type
     * @return Result containing deserialized object
     */
    public static <E, T> Result<E, T> fromJson(String json, Class<T> clazz) {
        return provider.fromJson(json, clazz);
    }

    /**
     * Type Alchemy: Converts any object to another type via JSON serialization.
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Map → POJO: convert(map, User.class)</li>
     *   <li>POJO → Map: convert(user, Map.class)</li>
     *   <li>List&lt;Map&gt; → List&lt;User&gt;: requires TypeReference (see provider-specific docs)</li>
     * </ul>
     *
     * @param from the source object
     * @param to the target class
     * @param <E> the error type
     * @param <T> the target type
     * @return Result containing converted object
     */
    public static <E, T> Result<E, T> convert(Object from, Class<T> to) {
        return provider.convert(from, to);
    }

    // ========== Tree Operations ==========

    /**
     * Converts an object to a navigable JSON tree (without serializing to string).
     *
     * @param value the object to convert
     * @param <E> the error type
     * @return Result containing JsonNodeWrapper
     */
    public static <E> Result<E, JsonNodeWrapper> toNode(Object value) {
        return provider.toNode(value);
    }

    /**
     * Parses JSON string directly to a navigable tree.
     *
     * @param json the JSON string
     * @param <E> the error type
     * @return Result containing JsonNodeWrapper
     */
    public static <E> Result<E, JsonNodeWrapper> parseNode(String json) {
        return provider.parseNode(json);
    }

    // ========== Safe Navigation ==========

    /**
     * Safely retrieves a string value from a JSON tree using path notation.
     *
     * <p><strong>Path Syntax:</strong></p>
     * <ul>
     *   <li>"users.0.name" → node.users[0].name</li>
     *   <li>"address.city" → node.address.city</li>
     *   <li>Returns Empty if any intermediate node is missing or null</li>
     * </ul>
     *
     * @param node the JSON node (or object that can be converted to node)
     * @param path the navigation path
     * @return Optional containing the string value, or Empty
     */
    public static Optional<String> getString(Object node, String path) {
        return provider.getString(node, path);
    }

    /**
     * Updates a value at the specified path in a JSON tree.
     *
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li>Creates intermediate nodes if they don't exist</li>
     *   <li>Overwrites existing values</li>
     *   <li>Returns a new tree (original is unchanged if immutable)</li>
     * </ul>
     *
     * @param node the JSON node to update
     * @param path the path to the value
     * @param value the new value
     * @param <E> the error type
     * @return Result containing updated tree
     */
    public static <E> Result<E, JsonNodeWrapper> updatePath(Object node, String path, Object value) {
        return provider.updatePath(node, path, value);
    }

    // ========== Advanced Operations ==========

    /**
     * Deep merges two JSON objects.
     *
     * <p><strong>Merge Rules:</strong></p>
     * <ul>
     *   <li>Objects are merged recursively (keys from both)</li>
     *   <li>Arrays are replaced (not concatenated)</li>
     *   <li>Primitives from 'update' overwrite 'main'</li>
     * </ul>
     *
     * @param main the base object
     * @param update the object to merge in
     * @param <E> the error type
     * @return Result containing merged tree
     */
    public static <E> Result<E, JsonNodeWrapper> merge(Object main, Object update) {
        return provider.merge(main, update);
    }

    /**
     * Recursively removes empty/null values from a JSON tree.
     *
     * <p><strong>Removes:</strong></p>
     * <ul>
     *   <li>null values</li>
     *   <li>Empty strings ("")</li>
     *   <li>Empty arrays ([])</li>
     *   <li>Empty objects ({})</li>
     * </ul>
     *
     * @param node the tree to prune
     * @return pruned tree
     */
    public static JsonNodeWrapper prune(Object node) {
        return provider.prune(node);
    }

    // ========== Convenience Methods ==========


    /**
     * Quick conversion of JSON string to Map.
     *
     * @param json the JSON string
     * @param <E> the error type
     * @return Result containing Map
     */
    @SuppressWarnings("unchecked")
    public static <E> Result<E, Map<String, Object>> toMap(String json) {
        return (Result<E, Map<String, Object>>) (Result<?, ?>) fromJson(json, Map.class);
    }

    /**
     * Quick conversion of JSON array string to List of Maps.
     *
     * @param json the JSON array string
     * @param <E> the error type
     * @return Result containing List of Maps
     */
    @SuppressWarnings("unchecked")
    public static <E> Result<E, List<Map<String, Object>>> toList(String json) {
        return (Result<E, List<Map<String, Object>>>) (Result<?, ?>) fromJson(json, List.class);
    }
    /**
     * Converts a JSON array node to a Java Stream for functional processing.
     *
     * <p><strong>Example:</strong></p>
     * <pre>
     * stream(arrayNode)
     *     .filter(node → node.get("age").asInt() > 18)
     *     .map(node → node.get("name").asText())
     *     .collect(Collectors.toList());
     * </pre>
     *
     * @param arrayNode the array node
     * @return Stream of JsonNodeWrapper, or empty stream if not an array
     */
    public static Stream<JsonNodeWrapper> stream(Object arrayNode) {
        return provider.stream(arrayNode);
    }
}
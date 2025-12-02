package commons.kit.JsonUtils;


import commons.kit.ErrorUtils.Result;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Service Provider Interface for JSON operations.
 *
 * <p>This interface allows swapping JSON implementations (Jackson, Gson, Moshi)
 * without changing client code.</p>
 *
 * <p><strong>Implementation Requirements:</strong></p>
 * <ul>
 *   <li>All methods must return Result (never throw exceptions)</li>
 *   <li>Null inputs should be handled gracefully</li>
 *   <li>Implementations should be thread-safe</li>
 * </ul>
 *
 * @author commons-kit
 * @version 1.3.0-SNAPSHOT
 */
public interface JsonProvider {

    /**
     * Serializes an object to JSON string.
     *
     * @param value the object to serialize
     * @param <E> the error type
     * @return Result containing JSON string or error
     */
    <E> Result<E, String> toJson(Object value);

    /**
     * Deserializes JSON string to an object.
     *
     * @param json the JSON string
     * @param clazz the target class
     * @param <E> the error type
     * @param <T> the target type
     * @return Result containing deserialized object or error
     */
    <E, T> Result<E, T> fromJson(String json, Class<T> clazz);

    /**
     * Converts an object to another type via JSON serialization.
     *
     * @param from the source object
     * @param to the target class
     * @param <E> the error type
     * @param <T> the target type
     * @return Result containing converted object or error
     */
    <E, T> Result<E, T> convert(Object from, Class<T> to);

    /**
     * Converts an object to a JSON tree node.
     *
     * @param value the object to convert
     * @param <E> the error type
     * @return Result containing JsonNodeWrapper or error
     */
    <E> Result<E, JsonNodeWrapper> toNode(Object value);

    /**
     * Parses JSON string to a tree node.
     *
     * @param json the JSON string
     * @param <E> the error type
     * @return Result containing JsonNodeWrapper or error
     */
    <E> Result<E, JsonNodeWrapper> parseNode(String json);

    /**
     * Safely retrieves a string value using path notation.
     *
     * @param node the JSON node
     * @param path the navigation path (e.g., "users.0.name")
     * @return Optional containing the value, or Empty if not found
     */
    Optional<String> getString(Object node, String path);

    /**
     * Updates a value at the specified path.
     *
     * @param node the JSON node
     * @param path the path to update
     * @param value the new value
     * @param <E> the error type
     * @return Result containing updated node or error
     */
    <E> Result<E, JsonNodeWrapper> updatePath(Object node, String path, Object value);

    /**
     * Deep merges two JSON objects.
     *
     * @param main the base object
     * @param update the object to merge in
     * @param <E> the error type
     * @return Result containing merged node or error
     */
    <E> Result<E, JsonNodeWrapper> merge(Object main, Object update);

    /**
     * Removes null and empty values from a tree.
     *
     * @param node the tree to prune
     * @return pruned tree
     */
    JsonNodeWrapper prune(Object node);

    /**
     * Converts an array node to a Stream.
     *
     * @param arrayNode the array node
     * @return Stream of nodes, or empty stream if not an array
     */
    Stream<JsonNodeWrapper> stream(Object arrayNode);
}

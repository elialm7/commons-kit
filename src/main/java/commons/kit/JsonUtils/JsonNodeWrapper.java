package commons.kit.JsonUtils;


/**
 * Wrapper interface for JSON nodes, abstracting implementation details.
 *
 * <p>This interface hides whether we're using Jackson's JsonNode, Gson's JsonElement,
 * or another implementation.</p>
 */
public interface JsonNodeWrapper {

    /**
     * Gets a child node by key (for objects) or index (for arrays).
     *
     * @param key the key or index
     * @return child node, or null if not found
     */
    JsonNodeWrapper get(String key);

    /**
     * Gets a node at the specified path.
     *
     * @param path the JSON pointer path (e.g., "/users/0/name")
     * @return node at path, or null if not found
     */
    JsonNodeWrapper at(String path);

    /**
     * Returns the text value of this node.
     *
     * @return text value, or null if not a text node
     */
    String asText();

    /**
     * Returns the integer value of this node.
     *
     * @return int value, or 0 if not a number
     */
    int asInt();

    /**
     * Returns the long value of this node.
     *
     * @return long value, or 0 if not a number
     */
    long asLong();

    /**
     * Returns the double value of this node.
     *
     * @return double value, or 0.0 if not a number
     */
    double asDouble();

    /**
     * Returns the boolean value of this node.
     *
     * @return boolean value, or false if not a boolean
     */
    boolean asBoolean();

    /**
     * Checks if this node is an array.
     *
     * @return true if array
     */
    boolean isArray();

    /**
     * Checks if this node is an object.
     *
     * @return true if object
     */
    boolean isObject();

    /**
     * Checks if this node is null.
     *
     * @return true if null
     */
    boolean isNull();

    /**
     * Returns an iterable of object keys.
     *
     * @return iterable of keys, or empty if not an object
     */
    Iterable<String> keys();

    /**
     * Returns the size of this array or object.
     *
     * @return size, or 0 if not a container
     */
    int size();

    /**
     * Returns the underlying implementation object.
     *
     * @param <T> the implementation type
     * @return the wrapped object
     */
    <T> T unwrap();
}
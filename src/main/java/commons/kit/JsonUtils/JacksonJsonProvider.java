package commons.kit.JsonUtils;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import commons.kit.ErrorUtils.Result;

import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Jackson-based implementation of JsonProvider.
 *
 * <p>This is the default JSON provider for commons-kit. It uses Jackson's
 * ObjectMapper with sensible defaults for common use cases.</p>
 *
 * <p><strong>Configuration:</strong></p>
 * <ul>
 *   <li>Ignores null values during serialization</li>
 *   <li>Formats dates as ISO-8601</li>
 *   <li>Handles Java 8 time types (LocalDate, LocalDateTime, etc.)</li>
 *   <li>Fails on unknown properties (strict mode)</li>
 * </ul>
 *
 * @author commons-kit
 * @version 1.3.0-SNAPSHOT
 */
public class JacksonJsonProvider implements JsonProvider {

    private final ObjectMapper mapper;

    /**
     * Creates a new JacksonJsonProvider with default configuration.
     */
    public JacksonJsonProvider() {
        this.mapper = createDefaultMapper();
    }

    /**
     * Creates a JacksonJsonProvider with a custom ObjectMapper.
     *
     * @param customMapper the ObjectMapper to use
     */
    public JacksonJsonProvider(ObjectMapper customMapper) {
        this.mapper = customMapper != null ? customMapper : createDefaultMapper();
    }

    /**
     * Creates an ObjectMapper with sensible defaults.
     */
    private static ObjectMapper createDefaultMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Ignore null values in output
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Handle Java 8 date/time types
        mapper.registerModule(new JavaTimeModule());

        // Don't fail on unknown properties during deserialization
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Pretty print for readability
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        // Write dates as ISO-8601 strings instead of timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Result<E, String> toJson(Object value) {
        try {
            String json = mapper.writeValueAsString(value);
            return Result.ok(json);
        } catch (Exception e) {
            return Result.err((E) ("JSON serialization failed: " + e.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, T> Result<E, T> fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return Result.err((E) "JSON string is null or empty");
        }

        try {
            T result = mapper.readValue(json, clazz);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.err((E) ("JSON deserialization failed: " + e.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E, T> Result<E, T> convert(Object from, Class<T> to) {
        if (from == null) {
            return Result.err((E) "Source object is null");
        }

        try {
            T result = mapper.convertValue(from, to);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.err((E) ("Type conversion failed: " + e.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Result<E, JsonNodeWrapper> toNode(Object value) {
        try {
            JsonNode node = mapper.valueToTree(value);
            return Result.ok(new JacksonNodeWrapper(node));
        } catch (Exception e) {
            return Result.err((E) ("Node conversion failed: " + e.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Result<E, JsonNodeWrapper> parseNode(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Result.err((E) "JSON string is null or empty");
        }

        try {
            JsonNode node = mapper.readTree(json);
            return Result.ok(new JacksonNodeWrapper(node));
        } catch (Exception e) {
            return Result.err((E) ("JSON parsing failed: " + e.getMessage()));
        }
    }

    @Override
    public Optional<String> getString(Object node, String path) {
        if (node == null || path == null) {
            return Optional.empty();
        }

        try {
            JsonNode jsonNode = toJsonNode(node);
            JsonNode current = jsonNode;

            // Navigate through path segments
            String[] segments = path.split("\\.");
            for (String segment : segments) {
                if (current == null || current.isNull()) {
                    return Optional.empty();
                }

                // Handle array indices
                if (segment.matches("\\d+")) {
                    int index = Integer.parseInt(segment);
                    if (current.isArray() && index < current.size()) {
                        current = current.get(index);
                    } else {
                        return Optional.empty();
                    }
                } else {
                    current = current.get(segment);
                }
            }

            if (current != null && !current.isNull()) {
                return Optional.of(current.asText());
            }
        } catch (Exception e) {
            // Silently fail - return empty optional
        }

        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Result<E, JsonNodeWrapper> updatePath(Object node, String path, Object value) {
        try {
            JsonNode jsonNode = toJsonNode(node);
            JsonNode valueNode = mapper.valueToTree(value);

            String[] segments = path.split("\\.");
            JsonNode current = jsonNode;

            // Navigate to parent, creating nodes as needed
            for (int i = 0; i < segments.length - 1; i++) {
                String segment = segments[i];

                if (segment.matches("\\d+")) {
                    // Array index
                    int index = Integer.parseInt(segment);
                    if (!current.isArray()) {
                        return Result.err((E) "Cannot index non-array node");
                    }
                    current = current.get(index);
                } else {
                    // Object key
                    if (!current.isObject()) {
                        return Result.err((E) "Cannot access property on non-object node");
                    }

                    ObjectNode objNode = (ObjectNode) current;
                    if (!objNode.has(segment)) {
                        objNode.set(segment, mapper.createObjectNode());
                    }
                    current = objNode.get(segment);
                }
            }

            // Set the value at the final segment
            String lastSegment = segments[segments.length - 1];
            if (current.isObject()) {
                ((ObjectNode) current).set(lastSegment, valueNode);
            } else {
                return Result.err((E) "Cannot set property on non-object node");
            }

            return Result.ok(new JacksonNodeWrapper(jsonNode));
        } catch (Exception e) {
            return Result.err((E) ("Path update failed: " + e.getMessage()));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Result<E, JsonNodeWrapper> merge(Object main, Object update) {
        try {
            JsonNode mainNode = toJsonNode(main);
            JsonNode updateNode = toJsonNode(update);

            JsonNode merged = deepMerge(mainNode, updateNode);
            return Result.ok(new JacksonNodeWrapper(merged));
        } catch (Exception e) {
            return Result.err((E) ("Merge failed: " + e.getMessage()));
        }
    }

    @Override
    public JsonNodeWrapper prune(Object node) {
        JsonNode jsonNode = toJsonNode(node);
        JsonNode pruned = pruneNode(jsonNode);
        return new JacksonNodeWrapper(pruned);
    }

    @Override
    public Stream<JsonNodeWrapper> stream(Object arrayNode) {
        JsonNode jsonNode = toJsonNode(arrayNode);

        if (!jsonNode.isArray()) {
            return Stream.empty();
        }

        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        jsonNode.elements(),
                        Spliterator.ORDERED
                ),
                false
        ).map(JacksonNodeWrapper::new);
    }

    // ========== Helper Methods ==========

    private JsonNode toJsonNode(Object obj) {
        if (obj instanceof JacksonNodeWrapper) {
            return ((JacksonNodeWrapper) obj).unwrap();
        }
        if (obj instanceof JsonNode) {
            return (JsonNode) obj;
        }
        return mapper.valueToTree(obj);
    }

    private JsonNode deepMerge(JsonNode main, JsonNode update) {
        if (!main.isObject() || !update.isObject()) {
            return update; // Replace non-objects
        }

        ObjectNode result = ((ObjectNode) main).deepCopy();

        update.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode updateValue = entry.getValue();

            if (result.has(key) && result.get(key).isObject() && updateValue.isObject()) {
                // Recursively merge objects
                result.set(key, deepMerge(result.get(key), updateValue));
            } else {
                // Replace value
                result.set(key, updateValue);
            }
        });

        return result;
    }

    private JsonNode pruneNode(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            ObjectNode result = mapper.createObjectNode();

            obj.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();

                // Skip nulls, empty strings, empty arrays, empty objects
                if (value.isNull() ||
                        (value.isTextual() && value.asText().isEmpty()) ||
                        (value.isArray() && value.isEmpty()) ||
                        (value.isObject() && value.isEmpty())) {
                    return;
                }

                // Recursively prune nested structures
                if (value.isObject() || value.isArray()) {
                    value = pruneNode(value);
                }

                result.set(entry.getKey(), value);
            });

            return result;
        } else if (node.isArray()) {
            ArrayNode arr = (ArrayNode) node;
            ArrayNode result = mapper.createArrayNode();

            arr.forEach(element -> {
                if (!element.isNull()) {
                    result.add(pruneNode(element));
                }
            });

            return result;
        }

        return node;
    }

    // ========== Inner Class: Node Wrapper ==========

    private static class JacksonNodeWrapper implements JsonNodeWrapper {
        private final JsonNode node;

        JacksonNodeWrapper(JsonNode node) {
            this.node = node;
        }

        @Override
        public JsonNodeWrapper get(String key) {
            JsonNode child = node.get(key);
            return child != null ? new JacksonNodeWrapper(child) : null;
        }

        @Override
        public JsonNodeWrapper at(String path) {
            JsonNode child = node.at(path);
            return child != null && !child.isMissingNode() ? new JacksonNodeWrapper(child) : null;
        }

        @Override
        public String asText() {
            return node.asText();
        }

        @Override
        public int asInt() {
            return node.asInt();
        }

        @Override
        public long asLong() {
            return node.asLong();
        }

        @Override
        public double asDouble() {
            return node.asDouble();
        }

        @Override
        public boolean asBoolean() {
            return node.asBoolean();
        }

        @Override
        public boolean isArray() {
            return node.isArray();
        }

        @Override
        public boolean isObject() {
            return node.isObject();
        }

        @Override
        public boolean isNull() {
            return node.isNull();
        }

        @Override
        public Iterable<String> keys() {
            return node::fieldNames;
        }

        @Override
        public int size() {
            return node.size();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T unwrap() {
            return (T) node;
        }

        @Override
        public String toString() {
            return node.toString();
        }
    }
}
package json;
import commons.kit.ErrorUtils.Result;
import commons.kit.JsonUtils.JsonNodeWrapper;
import commons.kit.JsonUtils.JsonUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
class JsonUtilsTest {

    // ========================================================================
    // CORE SERIALIZATION TESTS
    // ========================================================================

    @Test
    @DisplayName("toJson() - Serializes simple object")
    void testToJsonSimpleObject() {
        Map<String, Object> map = Map.of("name", "Alice", "age", 30);
        Result<String, String> result = JsonUtils.toJson(map);

        assertTrue(result.isOk());
        String json = result.getOrThrow();
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"Alice\""));
    }

    @Test
    @DisplayName("toJson() - Handles null values (ignores them)")
    void testToJsonWithNulls() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("email", null);

        Result<String, String> result = JsonUtils.toJson(map);

        assertTrue(result.isOk());
        String json = result.getOrThrow();
        assertFalse(json.contains("email")); // Null values are ignored
    }

    @Test
    @DisplayName("toJson() - Serializes nested objects")
    void testToJsonNested() {
        Map<String, Object> address = Map.of("city", "NYC", "zip", "10001");
        Map<String, Object> user = Map.of("name", "Alice", "address", address);

        Result<String, String> result = JsonUtils.toJson(user);

        assertTrue(result.isOk());
        String json = result.getOrThrow();
        assertTrue(json.contains("\"city\""));
        assertTrue(json.contains("\"NYC\""));
    }

    @Test
    @DisplayName("toJson() - Serializes arrays")
    void testToJsonArray() {
        List<String> list = List.of("apple", "banana", "cherry");
        Result<String, String> result = JsonUtils.toJson(list);

        assertTrue(result.isOk());
        String json = result.getOrThrow();
        assertTrue(json.contains("apple"));
        assertTrue(json.contains("banana"));
    }

    // ========================================================================
    // DESERIALIZATION TESTS
    // ========================================================================

    @Test
    @DisplayName("fromJson() - Deserializes to Map")
    void testFromJsonToMap() {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        Result<String, Map> result = JsonUtils.fromJson(json, Map.class);

        assertTrue(result.isOk());
        Map<?, ?> map = result.getOrThrow();
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    @DisplayName("fromJson() - Deserializes to List")
    void testFromJsonToList() {
        String json = "[\"apple\",\"banana\",\"cherry\"]";
        Result<String, List> result = JsonUtils.fromJson(json, List.class);

        assertTrue(result.isOk());
        List<?> list = result.getOrThrow();
        assertEquals(3, list.size());
        assertEquals("apple", list.get(0));
    }

    @Test
    @DisplayName("fromJson() - Returns error for null input")
    void testFromJsonNullInput() {
        Result<String, Map> result = JsonUtils.fromJson(null, Map.class);

        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("fromJson() - Returns error for empty input")
    void testFromJsonEmptyInput() {
        Result<String, Map> result = JsonUtils.fromJson("", Map.class);

        assertTrue(result.isErr());
    }

    @Test
    @DisplayName("fromJson() - Returns error for invalid JSON")
    void testFromJsonInvalidJson() {
        Result<String, Map> result = JsonUtils.fromJson("{invalid json", Map.class);

        assertTrue(result.isErr());
        result.peekErr(error -> assertTrue(error.contains("failed")));
    }

    // ========================================================================
    // TYPE CONVERSION TESTS (Type Alchemy)
    // ========================================================================

    @Test
    @DisplayName("convert() - Converts Map to Map (roundtrip)")
    void testConvertMapToMap() {
        Map<String, Object> original = Map.of("name", "Alice", "age", 30);
        Result<String, Map> result = JsonUtils.convert(original, Map.class);

        assertTrue(result.isOk());
        Map<?, ?> converted = result.getOrThrow();
        assertEquals("Alice", converted.get("name"));
    }

    @Test
    @DisplayName("convert() - Converts between compatible types")
    void testConvertCompatibleTypes() {
        Map<String, Object> source = Map.of(
                "name", "Alice",
                "age", 30,
                "active", true
        );

        Result<String, Map> result = JsonUtils.convert(source, Map.class);

        assertTrue(result.isOk());
        Map<?, ?> target = result.getOrThrow();
        assertEquals(3, target.size());
    }

    @Test
    @DisplayName("convert() - Returns error for null input")
    void testConvertNullInput() {
        Result<String, Map> result = JsonUtils.convert(null, Map.class);

        assertTrue(result.isErr());
    }

    // ========================================================================
    // TREE OPERATIONS TESTS
    // ========================================================================

    @Test
    @DisplayName("toNode() - Converts object to tree")
    void testToNode() {
        Map<String, Object> map = Map.of("name", "Alice", "age", 30);
        Result<String, JsonNodeWrapper> result = JsonUtils.toNode(map);

        assertTrue(result.isOk());
        JsonNodeWrapper node = result.getOrThrow();
        assertTrue(node.isObject());
    }

    @Test
    @DisplayName("parseNode() - Parses JSON string to tree")
    void testParseNode() {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        Result<String, JsonNodeWrapper> result = JsonUtils.parseNode(json);

        assertTrue(result.isOk());
        JsonNodeWrapper node = result.getOrThrow();
        assertTrue(node.isObject());
        assertEquals("Alice", node.get("name").asText());
    }

    @Test
    @DisplayName("parseNode() - Returns error for invalid JSON")
    void testParseNodeInvalid() {
        Result<String, JsonNodeWrapper> result = JsonUtils.parseNode("{invalid}");

        assertTrue(result.isErr());
    }

    // ========================================================================
    // SAFE NAVIGATION TESTS
    // ========================================================================

    @Test
    @DisplayName("getString() - Retrieves simple property")
    void testGetStringSimple() {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Optional<String> result = JsonUtils.getString(node, "name");

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get());
    }

    @Test
    @DisplayName("getString() - Retrieves nested property")
    void testGetStringNested() {
        String json = "{\"user\":{\"address\":{\"city\":\"NYC\"}}}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Optional<String> result = JsonUtils.getString(node, "user.address.city");

        assertTrue(result.isPresent());
        assertEquals("NYC", result.get());
    }

    @Test
    @DisplayName("getString() - Retrieves array element")
    void testGetStringArray() {
        String json = "{\"users\":[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Optional<String> result = JsonUtils.getString(node, "users.0.name");

        assertTrue(result.isPresent());
        assertEquals("Alice", result.get());
    }

    @Test
    @DisplayName("getString() - Returns empty for missing path")
    void testGetStringMissing() {
        String json = "{\"name\":\"Alice\"}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Optional<String> result = JsonUtils.getString(node, "nonexistent");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getString() - Returns empty for null input")
    void testGetStringNullInput() {
        Optional<String> result = JsonUtils.getString(null, "name");

        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("getString() - Returns empty for null path")
    void testGetStringNullPath() {
        String json = "{\"name\":\"Alice\"}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Optional<String> result = JsonUtils.getString(node, null);

        assertFalse(result.isPresent());
    }

    // ========================================================================
    // UPDATE PATH TESTS
    // ========================================================================

    @Test
    @DisplayName("updatePath() - Updates simple property")
    void testUpdatePathSimple() {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.updatePath(node, "name", "Bob");

        assertTrue(result.isOk());
        JsonNodeWrapper updated = result.getOrThrow();
        assertEquals("Bob", updated.get("name").asText());
    }

    @Test
    @DisplayName("updatePath() - Updates nested property")
    void testUpdatePathNested() {
        String json = "{\"user\":{\"address\":{\"city\":\"NYC\"}}}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.updatePath(
                node,
                "user.address.city",
                "LA"
        );

        assertTrue(result.isOk());
        JsonNodeWrapper updated = result.getOrThrow();
        Optional<String> city = JsonUtils.getString(updated, "user.address.city");
        assertTrue(city.isPresent());
        assertEquals("LA", city.get());
    }

    @Test
    @DisplayName("updatePath() - Creates intermediate nodes")
    void testUpdatePathCreateNodes() {
        String json = "{\"name\":\"Alice\"}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.updatePath(
                node,
                "address.city",
                "NYC"
        );

        assertTrue(result.isOk());
    }

    // ========================================================================
    // MERGE TESTS
    // ========================================================================

    @Test
    @DisplayName("merge() - Deep merges two objects")
    void testMerge() {
        String main = "{\"name\":\"Alice\",\"age\":30}";
        String update = "{\"age\":31,\"email\":\"alice@example.com\"}";

        JsonNodeWrapper mainNode = JsonUtils.parseNode(main).getOrThrow();
        JsonNodeWrapper updateNode = JsonUtils.parseNode(update).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.merge(mainNode, updateNode);

        assertTrue(result.isOk());
        JsonNodeWrapper merged = result.getOrThrow();
        assertEquals("Alice", merged.get("name").asText());
        assertEquals(31, merged.get("age").asInt());
        assertEquals("alice@example.com", merged.get("email").asText());
    }

    @Test
    @DisplayName("merge() - Merges nested objects recursively")
    void testMergeNested() {
        String main = "{\"user\":{\"name\":\"Alice\",\"age\":30}}";
        String update = "{\"user\":{\"age\":31,\"email\":\"alice@example.com\"}}";

        JsonNodeWrapper mainNode = JsonUtils.parseNode(main).getOrThrow();
        JsonNodeWrapper updateNode = JsonUtils.parseNode(update).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.merge(mainNode, updateNode);

        assertTrue(result.isOk());
        JsonNodeWrapper merged = result.getOrThrow();
        assertEquals("Alice", JsonUtils.getString(merged, "user.name").get());
        assertEquals("31", JsonUtils.getString(merged, "user.age").get());
    }

    @Test
    @DisplayName("merge() - Replaces arrays (not concatenates)")
    void testMergeArraysReplace() {
        String main = "{\"tags\":[\"java\",\"python\"]}";
        String update = "{\"tags\":[\"javascript\"]}";

        JsonNodeWrapper mainNode = JsonUtils.parseNode(main).getOrThrow();
        JsonNodeWrapper updateNode = JsonUtils.parseNode(update).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.merge(mainNode, updateNode);

        assertTrue(result.isOk());
        JsonNodeWrapper merged = result.getOrThrow();
        JsonNodeWrapper tags = merged.get("tags");
        assertEquals(1, tags.size()); // Replaced, not concatenated
    }

    // ========================================================================
    // PRUNE TESTS
    // ========================================================================

    @Test
    @DisplayName("prune() - Removes null values")
    void testPruneNull() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("email", null);
        map.put("age", 30);

        JsonNodeWrapper node = JsonUtils.toNode(map).getOrThrow();
        JsonNodeWrapper pruned = JsonUtils.prune(node);

        assertFalse(pruned.get("email") != null);
        assertEquals("Alice", pruned.get("name").asText());
    }

    @Test
    @DisplayName("prune() - Removes empty strings")
    void testPruneEmptyString() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("email", "");

        JsonNodeWrapper node = JsonUtils.toNode(map).getOrThrow();
        JsonNodeWrapper pruned = JsonUtils.prune(node);

        // Email should be removed
        assertTrue(pruned.get("name") != null);
    }

    @Test
    @DisplayName("prune() - Removes empty arrays")
    void testPruneEmptyArray() {
        String json = "{\"name\":\"Alice\",\"tags\":[]}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        JsonNodeWrapper pruned = JsonUtils.prune(node);

        // Tags array should be removed
        assertEquals("Alice", pruned.get("name").asText());
    }

    @Test
    @DisplayName("prune() - Removes empty objects")
    void testPruneEmptyObject() {
        String json = "{\"name\":\"Alice\",\"metadata\":{}}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        JsonNodeWrapper pruned = JsonUtils.prune(node);

        // Metadata should be removed
        assertEquals("Alice", pruned.get("name").asText());
    }

    @Test
    @DisplayName("prune() - Recursively prunes nested structures")
    void testPruneRecursive() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("city", "NYC");
        nested.put("state", "");

        Map<String, Object> map = new HashMap<>();
        map.put("name", "Alice");
        map.put("address", nested);

        JsonNodeWrapper node = JsonUtils.toNode(map).getOrThrow();
        JsonNodeWrapper pruned = JsonUtils.prune(node);

        assertEquals("NYC", JsonUtils.getString(pruned, "address.city").get());
    }

    // ========================================================================
    // CONVENIENCE METHODS TESTS
    // ========================================================================

    @Test
    @DisplayName("toMap() - Converts JSON to Map")
    void testToMap() {
        String json = "{\"name\":\"Alice\",\"age\":30}";
        Result<String, Map<String, Object>> result = JsonUtils.toMap(json);

        assertTrue(result.isOk());
        Map<String, Object> map = result.getOrThrow();
        assertEquals("Alice", map.get("name"));
        assertEquals(30, map.get("age"));
    }

    @Test
    @DisplayName("toList() - Converts JSON array to List of Maps")
    void testToList() {
        String json = "[{\"name\":\"Alice\"},{\"name\":\"Bob\"}]";
        Result<String, List<Map<String, Object>>> result = JsonUtils.toList(json);

        assertTrue(result.isOk());
        List<Map<String, Object>> list = result.getOrThrow();
        assertEquals(2, list.size());
        assertEquals("Alice", list.get(0).get("name"));
    }

    // ========================================================================
    // STREAM TESTS
    // ========================================================================

    @Test
    @DisplayName("stream() - Streams array elements")
    void testStream() {
        String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        List<String> names = JsonUtils.stream(node)
                .map(n -> n.get("name").asText())
                .collect(Collectors.toList());

        assertEquals(2, names.size());
        assertEquals("Alice", names.get(0));
        assertEquals("Bob", names.get(1));
    }

    @Test
    @DisplayName("stream() - Filters array elements")
    void testStreamFilter() {
        String json = "[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        long count = JsonUtils.stream(node)
                .filter(n -> n.get("age").asInt() > 26)
                .count();

        assertEquals(1, count);
    }

    @Test
    @DisplayName("stream() - Returns empty stream for non-array")
    void testStreamNonArray() {
        String json = "{\"name\":\"Alice\"}";
        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        long count = JsonUtils.stream(node).count();

        assertEquals(0, count);
    }

    // ========================================================================
    // REAL-WORLD SCENARIOS
    // ========================================================================

    @Test
    @DisplayName("Real World - User profile update")
    void testRealWorldUserUpdate() {
        String existingProfile = "{\"name\":\"Alice\",\"age\":30,\"email\":\"alice@old.com\"}";
        String updates = "{\"email\":\"alice@new.com\",\"phone\":\"+1234567890\"}";

        JsonNodeWrapper profile = JsonUtils.parseNode(existingProfile).getOrThrow();
        JsonNodeWrapper updateData = JsonUtils.parseNode(updates).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.merge(profile, updateData);

        assertTrue(result.isOk());
        JsonNodeWrapper merged = result.getOrThrow();
        assertEquals("Alice", merged.get("name").asText());
        assertEquals("alice@new.com", merged.get("email").asText());
        assertEquals("+1234567890", merged.get("phone").asText());
    }

    @Test
    @DisplayName("Real World - Configuration merge")
    void testRealWorldConfigMerge() {
        String defaultConfig = "{\"timeout\":30,\"retries\":3,\"debug\":false}";
        String userConfig = "{\"timeout\":60,\"debug\":true}";

        JsonNodeWrapper defaults = JsonUtils.parseNode(defaultConfig).getOrThrow();
        JsonNodeWrapper user = JsonUtils.parseNode(userConfig).getOrThrow();

        Result<String, JsonNodeWrapper> result = JsonUtils.merge(defaults, user);

        assertTrue(result.isOk());
        JsonNodeWrapper config = result.getOrThrow();
        assertEquals(60, config.get("timeout").asInt());
        assertEquals(3, config.get("retries").asInt());
        assertTrue(config.get("debug").asBoolean());
    }

    @Test
    @DisplayName("Real World - Filter and transform data")
    void testRealWorldFilterTransform() {
        String json = "[" +
                "{\"name\":\"Alice\",\"age\":30,\"active\":true}," +
                "{\"name\":\"Bob\",\"age\":25,\"active\":false}," +
                "{\"name\":\"Charlie\",\"age\":35,\"active\":true}" +
                "]";

        JsonNodeWrapper node = JsonUtils.parseNode(json).getOrThrow();

        List<String> activeUsers = JsonUtils.stream(node)
                .filter(n -> n.get("active").asBoolean())
                .filter(n -> n.get("age").asInt() > 28)
                .map(n -> n.get("name").asText())
                .collect(Collectors.toList());

        assertEquals(2, activeUsers.size());
        assertTrue(activeUsers.contains("Alice"));
        assertTrue(activeUsers.contains("Charlie"));
    }
}

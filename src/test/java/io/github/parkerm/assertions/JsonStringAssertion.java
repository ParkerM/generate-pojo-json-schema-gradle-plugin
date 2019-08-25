package io.github.parkerm.assertions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.util.Streams;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class JsonStringAssertion extends AbstractAssert<JsonStringAssertion, String> {

    private static final ObjectMapper mapper = new ObjectMapper();
    private JsonNode jsonNode;

    private JsonStringAssertion(String actual) {
        super(actual, JsonStringAssertion.class);
        try {
            jsonNode = mapper.readTree(actual);
        } catch (IOException e) {
            Assertions.fail("Error parsing JSON String.", e);
        }
    }

    public static JsonStringAssertion assertThat(String actual) {
        return new JsonStringAssertion(actual);
    }

    public JsonStringAssertion extractingNode(String key) {
        hasField(key);
        jsonNode = jsonNode.get(key);
        return this;
    }

    public IterableAssert<String> withArrayStringField(String key) {
        hasField(key);
        JsonNode found = jsonNode.get(key);
        if (!found.isArray()) {
            failWithMessage("Value is not instance of ArrayNode. Found: %s", found.toString());
        }

        ArrayNode foundArray = (ArrayNode) found;
        List<String> elements = Streams.stream(foundArray.elements())
                .map(JsonNode::asText)
                .collect(Collectors.toList());

        return new IterableAssert<>(elements);
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonStringAssertion hasField(String key) {
        isNotNull();
        JsonNode found = jsonNode.get(key);
        if (found == null) {
            failWithMessage("Expected\n " +
                    "<%s>\n " +
                    "to have field with name: \"%s\"", actual, key);
        }
        return this;
    }

    @SuppressWarnings("UnusedReturnValue")
    public JsonStringAssertion doesNotHaveField(String key) {
        isNotNull();
        JsonNode found = jsonNode.get(key);
        if (found != null) {
            failWithMessage("Expected no field with name \"%s\".\n" +
                    "Found:\n" +
                    "\"%s\": %s", key, key, found.toString());
        }
        return this;
    }
}

package se.sundsvall.messaging.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.lang.reflect.Type;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static tools.jackson.databind.type.TypeFactory.rawClass;

public final class JsonUtils {

	// WRITE_DATES_AS_TIMESTAMPS and ADJUST_DATES_TO_CONTEXT_TIME_ZONE are false by default in Jackson 3.0
	private static final ObjectMapper OBJECT_MAPPER = JsonMapper
		.builder(JsonFactory.builder()
			.streamReadConstraints(StreamReadConstraints.builder().maxStringLength(Integer.MAX_VALUE).build())
			.build())
		.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
		.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
		.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false).build();

	private JsonUtils() {}

	/**
	 * Serializes the given value to JSON.
	 *
	 * @param  value
	 *               the value to serialize
	 *
	 * @return       JSON
	 */
	public static String toJson(final Object value) {
		if (isNull(value)) {
			return null;
		}

		return OBJECT_MAPPER.writeValueAsString(value);
	}

	/**
	 * Deserializes the given JSON string to an object of the given type.
	 *
	 * @param  json
	 *                   the JSON string to deserialize
	 * @param  valueType
	 *                   the target type
	 *
	 * @return           a deserialized object
	 */
	public static <T> T fromJson(final String json, final Type valueType) {
		@SuppressWarnings("unchecked")
		final var valueTypeClass = (Class<T>) rawClass(valueType);

		return fromJson(json, valueTypeClass);
	}

	public static <T> T fromJson(final String json, final TypeReference<T> typeReference) {
		if (isBlank(json)) {
			return null;
		}

		return OBJECT_MAPPER.readValue(json, typeReference);
	}

	public static <T> T fromJson(final String json, final Class<T> valueType) {
		if (isBlank(json)) {
			return null;
		}

		return OBJECT_MAPPER.readValue(json, valueType);
	}
}

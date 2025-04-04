package se.sundsvall.messaging.util;

import static com.fasterxml.jackson.databind.type.TypeFactory.rawClass;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactoryBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.StreamReadConstraints;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.lang.reflect.Type;

public final class JsonUtils {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(
		new JsonFactoryBuilder()
			.streamReadConstraints(
				StreamReadConstraints
					.builder()
					.maxStringLength(Integer.MAX_VALUE)
					.build())
			.build())
		.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL)
		.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
		.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
		.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
		.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
		.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
		.registerModule(new JavaTimeModule());

	private JsonUtils() {}

	/**
	 * Serializes the given value to JSON.
	 *
	 * @param  value the value to serialize
	 * @return       JSON
	 */
	public static String toJson(final Object value) {
		if (isNull(value)) {
			return null;
		}

		try {
			return OBJECT_MAPPER.writeValueAsString(value);
		} catch (final JsonProcessingException e) {
			throw new RuntimeJsonProcessingException(e);
		}
	}

	/**
	 * Deserializes the given JSON string to an object of the given type.
	 *
	 * @param  json      the JSON string to deserialize
	 * @param  valueType the target type
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

		try {
			return OBJECT_MAPPER.readValue(json, typeReference);
		} catch (final JsonProcessingException e) {
			throw new RuntimeJsonProcessingException(e);
		}
	}

	public static <T> T fromJson(final String json, final Class<T> valueType) {
		if (isBlank(json)) {
			return null;
		}

		try {
			return OBJECT_MAPPER.readValue(json, valueType);
		} catch (final JsonProcessingException e) {
			throw new RuntimeJsonProcessingException(e);
		}
	}

	/**
	 * Unchecked variant/wrapper of {@link JsonProcessingException}.
	 */
	static class RuntimeJsonProcessingException extends RuntimeException {

		private static final long serialVersionUID = 2058498165913771606L;

		RuntimeJsonProcessingException(final JsonProcessingException cause) {
			super(cause);
		}
	}
}

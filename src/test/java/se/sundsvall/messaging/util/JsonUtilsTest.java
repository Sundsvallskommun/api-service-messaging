package se.sundsvall.messaging.util;

import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

class JsonUtilsTest {

	@Test
	void toJson() {
		final var person = new Person("Bob", "bob@something.com");

		assertThat(JsonUtils.toJson(person)).isEqualTo("{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}");
	}

	@Test
	void toJsonWhenValueIsNull() {
		assertThat(JsonUtils.toJson(null)).isNull();
	}

	@Test
	void fromJsonUsingClass() {
		final var json = "{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}";

		final var person = JsonUtils.fromJson(json, Person.class);

		assertThat(person).isNotNull();
		assertThat(person.name).isEqualTo("Bob");
		assertThat(person.emailAddress).isEqualTo("bob@something.com");
	}

	@Test
	void fromJsonWithNullJsonUsingClass() {
		assertThat(JsonUtils.fromJson(null, Person.class)).isNull();
	}

	@Test
	void fromJsonWithBlankJsonUsingClass() {
		assertThat(JsonUtils.fromJson("", Person.class)).isNull();
	}

	@Test
	void fromJsonWithInvalidJsonUsingClass() {
		assertThatExceptionOfType(JacksonException.class)
			.isThrownBy(() -> JsonUtils.fromJson("{", Person.class));
	}

	@Test
	void fromJsonUsingTypeReference() {
		final var json = "{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}";

		final var person = JsonUtils.fromJson(json, new TypeReference<Person>() {
		});

		assertThat(person).isNotNull();
		assertThat(person.name).isEqualTo("Bob");
		assertThat(person.emailAddress).isEqualTo("bob@something.com");
	}

	@Test
	void fromJsonWithNullJsonUsingTypeReference() {
		assertThat(JsonUtils.fromJson(null, new TypeReference<Person>() {
		})).isNull();
	}

	@Test
	void fromJsonWithBlankJsonUsingTypeReference() {
		assertThat(JsonUtils.fromJson("", new TypeReference<Person>() {
		})).isNull();
	}

	@Test
	void fromJsonWithInvalidJsonUsingTypeReference() {
		final var typeReference = new TypeReference<Person>() {
		};

		assertThatExceptionOfType(JacksonException.class)
			.isThrownBy(() -> JsonUtils.fromJson("{", typeReference));
	}

	record Person(String name, String emailAddress) {
	}
}

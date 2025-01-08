package se.sundsvall.messaging.util;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;

class JsonUtilsTest {

	record Person(String name, String emailAddress) {}

	@Test
	void toJson() {
		var person = new Person("Bob", "bob@something.com");

		assertThat(JsonUtils.toJson(person)).isEqualTo("{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}");
	}

	@Test
	void toJsonWhenValueIsNull() {
		assertThat(JsonUtils.toJson(null)).isNull();
	}

	@Test
	void fromJsonUsingClass() {
		var json = "{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}";

		var person = JsonUtils.fromJson(json, Person.class);

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
		assertThatExceptionOfType(JsonUtils.RuntimeJsonProcessingException.class)
			.isThrownBy(() -> JsonUtils.fromJson("{", Person.class));
	}

	@Test
	void fromJsonUsingTypeReference() {
		var json = "{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}";

		var person = JsonUtils.fromJson(json, new TypeReference<Person>() {});

		assertThat(person).isNotNull();
		assertThat(person.name).isEqualTo("Bob");
		assertThat(person.emailAddress).isEqualTo("bob@something.com");
	}

	@Test
	void fromJsonWithNullJsonUsingTypeReference() {
		assertThat(JsonUtils.fromJson(null, new TypeReference<Person>() {})).isNull();
	}

	@Test
	void fromJsonWithBlankJsonUsingTypeReference() {
		assertThat(JsonUtils.fromJson("", new TypeReference<Person>() {})).isNull();
	}

	@Test
	void fromJsonWithInvalidJsonUsingTypeReference() {
		assertThatExceptionOfType(JsonUtils.RuntimeJsonProcessingException.class)
			.isThrownBy(() -> JsonUtils.fromJson("{", new TypeReference<Person>() {}));
	}
}

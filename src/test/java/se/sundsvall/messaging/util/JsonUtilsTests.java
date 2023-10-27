package se.sundsvall.messaging.util;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class JsonUtilsTests {

    record Person(String name, String emailAddress) { }

    @Test
    void toJson() {
        var person = new Person("Bob", "bob@something.com");

        assertThat(JsonUtils.toJson(person)).isEqualTo("{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}");
    }

    @Test
    void fromJson() {
        var json = "{\"name\":\"Bob\",\"emailAddress\":\"bob@something.com\"}";

        var person = JsonUtils.fromJson(json, Person.class);

        assertThat(person).isNotNull();
        assertThat(person.name).isEqualTo("Bob");
        assertThat(person.emailAddress).isEqualTo("bob@something.com");
    }

    @Test
    void fromJsonWithNullJson() {
        assertThat(JsonUtils.fromJson(null, Person.class)).isNull();
    }

    @Test
    void fromJsonWithBlankJson() {
        assertThat(JsonUtils.fromJson("", Person.class)).isNull();
    }

    @Test
    void fromJsonWithInvalidJson() {
        assertThatExceptionOfType(JsonUtils.RuntimeJsonProcessingException.class)
            .isThrownBy(() -> JsonUtils.fromJson("{", Person.class));
    }
}

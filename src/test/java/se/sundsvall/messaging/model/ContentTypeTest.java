package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static se.sundsvall.messaging.model.ContentType.APPLICATION_PDF;
import static se.sundsvall.messaging.model.ContentType.TEXT_HTML;
import static se.sundsvall.messaging.model.ContentType.TEXT_PLAIN;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class ContentTypeTest {

	@Test
	void testEnumValues() {
		assertThat(ContentType.values()).containsExactlyInAnyOrder(
			APPLICATION_PDF,
			TEXT_HTML,
			TEXT_PLAIN);
	}

	@Test
	void testFromString() {
		assertThat(ContentType.fromString("text/plain")).isEqualTo(TEXT_PLAIN);
		assertThat(ContentType.fromString("text/html")).isEqualTo(TEXT_HTML);
		assertThat(ContentType.fromString("application/pdf")).isEqualTo(APPLICATION_PDF);
	}

	@Test
	void testFromStringWithInvalidValue() {
		assertThatExceptionOfType(IllegalArgumentException.class)
			.isThrownBy(() -> ContentType.fromString("somePhonyContentType"))
			.withMessageContaining("Unhandled content type");
	}
}

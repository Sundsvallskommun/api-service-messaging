package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class ContentTypeTest {

    @Test
    void testFromString() {
        var contentType = ContentType.fromString("text/plain");

        assertThat(contentType).isEqualTo(ContentType.TEXT_PLAIN);
    }

    @Test
    void testFromStringWithInvalidValue() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ContentType.fromString("somePhonyContentType"))
            .withMessageContaining("Unhandled content type");
    }
}

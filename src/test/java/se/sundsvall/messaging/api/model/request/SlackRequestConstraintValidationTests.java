package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SlackRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SlackRequestConstraintValidationTests {

    private final SlackRequest validRequest = createValidSlackRequest();

    @Test
    void shouldPassForValidRequest() {
        assertThat(validRequest).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithInvalidHeaders() {
        var header = validRequest.headers().get(0);

        // Test null header name
        var request = validRequest.withHeaders(List.of(header.withName(null)));

        assertThat(request)
            .hasSingleConstraintViolation("headers[0].name", message -> message.startsWith("must be one of"));

        // Test empty (null) header values
        request = validRequest.withHeaders(List.of(header.withValues(null)));

        assertThat(request)
            .hasSingleConstraintViolation("headers[0].values", "must not be empty");
    }

    @Test
    void shouldFailWithNullToken() {
        assertThat(validRequest.withToken(null))
            .hasSingleConstraintViolation("token", "must not be blank");
    }

    @Test
    void shouldFailWithBlankToken() {
        assertThat(validRequest.withToken(""))
            .hasSingleConstraintViolation("token", "must not be blank");
    }

    @Test
    void shouldFailWithNullChannel() {
        assertThat(validRequest.withChannel(null))
            .hasSingleConstraintViolation("channel", "must not be blank");
    }

    @Test
    void shouldFailWithBlankChannel() {
        assertThat(validRequest.withChannel(""))
            .hasSingleConstraintViolation("channel", "must not be blank");
    }

    @Test
    void shouldFailWithNullMessage() {
        assertThat(validRequest.withMessage(null))
            .hasSingleConstraintViolation("message", "must not be blank");
    }

    @Test
    void shouldFailWithBlankMessage() {
        assertThat(validRequest.withMessage(""))
            .hasSingleConstraintViolation("message", "must not be blank");
    }
}

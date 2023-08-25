package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SlackRequestAssertions.assertThat;

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

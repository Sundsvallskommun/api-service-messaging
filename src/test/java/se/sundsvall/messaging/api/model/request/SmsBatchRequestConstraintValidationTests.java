package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSmsBatchRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SmsBatchRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsBatchRequestConstraintValidationTests {

    @Test
    void shouldPassForValidRequest() {
		assertThat(createValidSmsBatchRequest()).hasNoConstraintViolations();
    }

    @Test
    void shouldPassWithoutParty() {
		final var request = createValidSmsBatchRequest();
		request.parties().getFirst().withPartyId(null);

		assertThat(request).hasNoConstraintViolations();
    }

    @Test
    void shouldFailWithInvalidPartyId() {
		final var request = createValidSmsBatchRequest()
			.withParties(List.of(SmsBatchRequest.Party.builder()
				.withPartyId("not-a-uuid")
				.withMobileNumber("+46701234567")
				.build()));

		assertThat(request).hasSingleConstraintViolation("parties[0].partyId", "not a valid UUID");
    }

    @Test
	void shouldFailWithNullMobileNumber() {
		final var request = createValidSmsBatchRequest()
			.withParties(List.of(SmsBatchRequest.Party.builder()
				.withMobileNumber(null)
				.build()));

		assertThat(request).hasSingleConstraintViolation("parties[0].mobileNumber", message -> message.startsWith("must be a valid MSISDN"));
    }

    @Test
	void shouldFailWithInvalidMobileNumber() {
		final var request = createValidSmsBatchRequest()
			.withParties(List.of(SmsBatchRequest.Party.builder()
				.withMobileNumber("not-a-mobile-number")
				.build()));

		assertThat(request).hasSingleConstraintViolation("parties[0].mobileNumber", message -> message.startsWith("must be a valid MSISDN"));
    }

    @Test
    void shouldFailWithNullMessage() {
		assertThat(createValidSmsBatchRequest().withMessage(null))
            .hasSingleConstraintViolation("message", "must not be blank");
    }

    @Test
    void shouldFailWithBlankMessage() {
		assertThat(createValidSmsBatchRequest().withMessage(""))
            .hasSingleConstraintViolation("message", "must not be blank");
    }
}

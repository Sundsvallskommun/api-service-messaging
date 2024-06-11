package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSmsBatchRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SmsBatchRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

	@ParameterizedTest
	@ValueSource(strings = {"ab", "1abc", "A_123456", "Abcdefghijkl", "   abc"})
	void shouldFailWithInvalidSender(final String sender) {
		assertThat(createValidSmsBatchRequest().withSender(sender))
			.hasSingleConstraintViolation("sender", "sender must be between 3-11 characters (allowed characters: a-z, A-Z, 0-9, whitespace) and start with a non-numeric character");
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "abc12", "Min bankman"})
	void shouldPassWithValidSender(final String sender) {
		assertThat(createValidSmsBatchRequest().withSender(sender)).hasNoConstraintViolations();
	}

	@Test
	void shouldFailWithNullSender() {
		assertThat(createValidSmsBatchRequest().withSender(null))
			.hasSingleConstraintViolation("sender", "must not be blank");
	}

	@Test
	void shouldFailWithBlankSender() {
		assertThat(createValidSmsBatchRequest().withSender(""))
			.hasConstraintViolation("sender", "must not be blank")
			.hasConstraintViolation("sender", "sender must be between 3-11 characters (allowed characters: a-z, A-Z, 0-9, whitespace) and start with a non-numeric character");
	}
}

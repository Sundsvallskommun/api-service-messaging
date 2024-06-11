package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.SmsRequestAssertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SmsRequestConstraintValidationTests {

	private final SmsRequest validRequest = createValidSmsRequest();

	@Test
	void shouldPassForValidRequest() {
		assertThat(validRequest).hasNoConstraintViolations();
	}

	@Test
	void shouldPassWithoutParty() {
		assertThat(validRequest.withParty(null)).hasNoConstraintViolations();
	}

	@Test
	void shouldFailWithInvalidPartyId() {
		var request = validRequest.withParty(validRequest.party().withPartyId("not-a-uuid"));

		assertThat(request).hasSingleConstraintViolation("party.partyId", "not a valid UUID");
	}

	@Test
	void shouldFailWithInvalidExternalReference() {
		var externalReference = validRequest.party().externalReferences().get(0);

		// Test invalid external reference key
		var request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withKey(null))));

		assertThat(request)
			.hasSingleConstraintViolation("party.externalReferences[0].key", "must not be blank");

		// Test invalid external reference value
		request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withValue(null))));

		assertThat(request)
			.hasSingleConstraintViolation("party.externalReferences[0].value", "must not be blank");
	}

	@Test
	void shouldFailWithNullMobileNumber() {
		assertThat(validRequest.withMobileNumber(null))
			.hasSingleConstraintViolation("mobileNumber", message -> message.startsWith("must be a valid MSISDN"));
	}

	@Test
	void shouldFailWithInvalidMobileNumber() {
		assertThat(validRequest.withMobileNumber("not-a-mobile-number"))
			.hasSingleConstraintViolation("mobileNumber", message -> message.startsWith("must be a valid MSISDN"));
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

	@ParameterizedTest
	@ValueSource(strings = {"ab", "1abc", "A_123456", "Abcdefghijklmnop"})
	void shouldFailWithInvalidSender(final String sender) {
		assertThat(validRequest.withSender(sender))
			.hasSingleConstraintViolation("sender", "sender must be between 3-11 characters and start with a non-numeric character");
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "abc12", "Min Bank"})
	void shouldPassWithValidSender(final String sender) {
		assertThat(validRequest.withSender(sender)).hasNoConstraintViolations();
	}

}

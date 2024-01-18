package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.REFERENCES;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.EmailRequestAssertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class EmailRequestConstraintValidationTests {

	private final EmailRequest validRequest = createValidEmailRequest();

	@Test
	void shouldPassForValidRequest() {
		assertThat(validRequest).hasNoConstraintViolations();
	}

	@Test
	void shouldPassWithNullParty() {
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
	void shouldFailWithNullEmailAddress() {
		assertThat(validRequest.withEmailAddress(null))
			.hasSingleConstraintViolation("emailAddress", "must not be blank");
	}

	@Test
	void shouldFailWithBlankEmailAddress() {
		assertThat(validRequest.withEmailAddress(" "))
			.hasConstraintViolation("emailAddress", "must not be blank")
			.hasConstraintViolation("emailAddress", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithInvalidEmailAddress() {
		assertThat(validRequest.withEmailAddress("not-an-email-address"))
			.hasSingleConstraintViolation("emailAddress", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithNullSubject() {
		assertThat(validRequest.withSubject(null))
			.hasSingleConstraintViolation("subject", "must not be blank");
	}

	@Test
	void shouldFailWithBlankSubject() {
		assertThat(validRequest.withSubject(" "))
			.hasSingleConstraintViolation("subject", "must not be blank");
	}

	@Test
	void shouldPassWithNullSender() {
		assertThat(validRequest.withSender(null)).hasNoConstraintViolations();
	}

	@Test
	void shouldFailWithSenderWithNullName() {
		var request = validRequest.withSender(validRequest.sender().withName(null));

		assertThat(request)
			.hasSingleConstraintViolation("sender.name", "must not be blank");
	}

	@Test
	void shouldFailWithSenderWithBlankName() {
		var request = validRequest.withSender(validRequest.sender().withName(" "));

		assertThat(request)
			.hasSingleConstraintViolation("sender.name", "must not be blank");
	}

	@Test
	void shouldFailWithSenderWithBlankAddress() {
		var request = validRequest.withSender(validRequest.sender().withAddress(" "));

		assertThat(request)
			.hasConstraintViolation("sender.address", "must not be blank")
			.hasConstraintViolation("sender.address", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithSenderWithInvalidAddress() {
		var request = validRequest.withSender(validRequest.sender().withAddress("not-an-email-address"));

		assertThat(request)
			.hasSingleConstraintViolation("sender.address", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithSenderWithInvalidReplyTo() {
		var request = validRequest.withSender(validRequest.sender().withReplyTo("not-an-email-address"));

		assertThat(request)
			.hasSingleConstraintViolation("sender.replyTo", "must be a well-formed email address");
	}

	@Test
	void shouldPassWithNullAttachments() {
		assertThat(validRequest.withAttachments(null)).hasNoConstraintViolations();
	}

	@Test
	void shouldPassWithEmptyAttachments() {
		assertThat(validRequest.withAttachments(List.of())).hasNoConstraintViolations();
	}

	@Test
	void shouldFailWithAttachmentWithNullName() {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withName(null)));

		assertThat(request)
			.hasSingleConstraintViolation("attachments[0].name", "must not be blank");
	}

	@Test
	void shouldFailWithAttachmentWithBlankName() {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withName(" ")));

		assertThat(request)
			.hasSingleConstraintViolation("attachments[0].name", "must not be blank");
	}

	@Test
	void shouldFailWithAttachmentWithNullContent() {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withContent(null)));

		assertThat(request)
			.hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
	}

	@Test
	void shouldFailWithAttachmentWithInvalidContent() {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withContent("not-base-64!!!")));

		assertThat(request)
			.hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
	}

	@ParameterizedTest
	@MethodSource("invalidHeaderArgumentProvider")
	void shouldFailWithInvalidHeaderValues(final Map<EmailRequest.Header, List<String>> map, final String field, final String message) {
		assertThat(validRequest.withHeaders(map))
			.hasSingleConstraintViolation(field, message);
	}

	private static Stream<Arguments> invalidHeaderArgumentProvider() {
		return Stream.of(
			Arguments.of(Map.of(MESSAGE_ID, List.of("not-a-va<lid-mes@sage->id")), "headers[MESSAGE_ID].<map value>[0].<list element>",
				"Header values must start with '<', contain '@' and end with '>'"),
			Arguments.of(Map.of(IN_REPLY_TO, List.of(">not-a-@valid-message-id<")), "headers[IN_REPLY_TO].<map value>[0].<list element>",
				"Header values must start with '<', contain '@' and end with '>'"),
			Arguments.of(Map.of(REFERENCES, List.of("<not-a-valid-message-id>")), "headers[REFERENCES].<map value>[0].<list element>",
				"Header values must start with '<', contain '@' and end with '>'"));
	}

}

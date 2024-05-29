package se.sundsvall.messaging.api.model.request;

import static se.sundsvall.messaging.TestDataFactory.createExternalReference;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestAttachment;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestParty;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestSender;
import static se.sundsvall.messaging.api.model.request.RequestValidationAssertions.EmailBatchRequestAssertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;

class EmailBatchRequestConstraintValidationTests {

	private final EmailBatchRequest validRequest = createValidEmailBatchRequest();
	private static final EmailBatchRequest.Party PARTY = createValidEmailBatchRequestParty();
	private static final EmailBatchRequest.Sender SENDER = createValidEmailBatchRequestSender();
	private static final EmailBatchRequest.Attachment ATTACHMENT = createValidEmailBatchRequestAttachment();
	private static final ExternalReference EXTERNAL_REFERENCE = createExternalReference();

	@Test
	void shouldPassForValidRequest() {
		assertThat(validRequest).hasNoConstraintViolations();
	}

	@Test
	void shouldPassWithNullParty() {
		assertThat(validRequest.withParties(null)).hasNoConstraintViolations();
	}

	@Test
	void shouldFailWithNullSubject() {
		assertThat(validRequest.withSubject(null)).hasSingleConstraintViolation("subject", "must not be blank");
	}

	@Test
	void shouldFailWithBlankSubject() {
		assertThat(validRequest.withSubject("")).hasSingleConstraintViolation("subject", "must not be blank");
	}

	@Test
	void shouldFailWithBlankHtmlMessage() {
		assertThat(validRequest.withHtmlMessage("")).hasSingleConstraintViolation("htmlMessage", "not a valid BASE64-encoded string");
	}

	@Test
	void shouldFailWithInvalidBase64EncodedHtmlMessage() {
		assertThat(validRequest.withHtmlMessage("not base64")).hasSingleConstraintViolation("htmlMessage", "not a valid BASE64-encoded string");
	}

	@Test
	void shouldFailWithNullPartyEmailAddress() {
		assertThat(validRequest.withParties(List.of(PARTY.withEmailAddress(null)))).hasSingleConstraintViolation("parties[0].emailAddress", "must not be blank");
	}

	@Test
	void shouldFailWithBlankPartyEmailAddress() {
		assertThat(validRequest.withParties(List.of(PARTY.withEmailAddress("")))).hasSingleConstraintViolation("parties[0].emailAddress", "must not be blank");
	}

	@Test
	void shouldFailWithInvalidPartyEmailAddress() {
		assertThat(validRequest.withParties(List.of(PARTY.withEmailAddress("Not valid")))).hasSingleConstraintViolation("parties[0].emailAddress", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithInvalidPartyId() {
		assertThat(validRequest.withParties(List.of(PARTY.withPartyId("not uuid")))).hasSingleConstraintViolation("parties[0].partyId", "not a valid UUID");
	}

	@Test
	void shouldFailWithInvalidSenderReplyTo() {
		assertThat(validRequest.withSender(SENDER.withReplyTo("null"))).hasSingleConstraintViolation("sender.replyTo", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithNullSenderName() {
		assertThat(validRequest.withSender(SENDER.withName(null))).hasSingleConstraintViolation("sender.name", "must not be blank");
	}

	@Test
	void shouldFailWithBlankSenderName() {
		assertThat(validRequest.withSender(SENDER.withName(""))).hasSingleConstraintViolation("sender.name", "must not be blank");
	}

	@Test
	void shouldFailWithNullSenderAddress() {
		assertThat(validRequest.withSender(SENDER.withAddress(null))).hasSingleConstraintViolation("sender.address", "must not be blank");
	}

	@Test
	void shouldFailWithBlankSenderAddress() {
		assertThat(validRequest.withSender(SENDER.withAddress(""))).hasSingleConstraintViolation("sender.address", "must not be blank");
	}

	@Test
	void shouldFailWithInvalidSenderAddress() {
		assertThat(validRequest.withSender(SENDER.withAddress("not an email"))).hasSingleConstraintViolation("sender.address", "must be a well-formed email address");
	}

	@Test
	void shouldFailWithNullAttachmentContent() {
		assertThat(validRequest.withAttachments(List.of(ATTACHMENT.withContent(null)))).hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
	}

	@Test
	void shouldFailWithInvalidAttachmentContent() {
		assertThat(validRequest.withAttachments(List.of(ATTACHMENT.withContent("not base64")))).hasSingleConstraintViolation("attachments[0].content", "not a valid BASE64-encoded string");
	}

	@Test
	void shouldFailWithNullAttachmentName() {
		assertThat(validRequest.withAttachments(List.of(ATTACHMENT.withName(null)))).hasSingleConstraintViolation("attachments[0].name", "must not be blank");
	}

	@Test
	void shouldFailWithBlankAttachmentName() {
		assertThat(validRequest.withAttachments(List.of(ATTACHMENT.withName("")))).hasSingleConstraintViolation("attachments[0].name", "must not be blank");
	}

	@Test
	void shouldFailWithInvalidMessageIdHeader() {
		assertThat(validRequest.withHeaders(Map.of(Header.MESSAGE_ID, List.of("not a valid message id")))).hasSingleConstraintViolation("headers[MESSAGE_ID].<map value>[0].<list element>", "Header values must start with '<', contain '@' and end with '>'");
	}

	@Test
	void shouldFailWithInvalidInReplyToHeader() {
		assertThat(validRequest.withHeaders(Map.of(Header.IN_REPLY_TO, List.of("not a valid in reply to")))).hasSingleConstraintViolation("headers[IN_REPLY_TO].<map value>[0].<list element>", "Header values must start with '<', contain '@' and end with '>'");
	}

	@Test
	void shouldFailWithInvalidReferencesHeader() {
		assertThat(validRequest.withHeaders(Map.of(Header.REFERENCES, List.of("not a valid reference")))).hasSingleConstraintViolation("headers[REFERENCES].<map value>[0].<list element>", "Header values must start with '<', contain '@' and end with '>'");
	}

}

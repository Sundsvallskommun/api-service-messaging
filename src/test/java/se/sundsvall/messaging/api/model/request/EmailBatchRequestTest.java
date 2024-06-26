package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class EmailBatchRequestTest {

	private final String origin = "origin";
	private final String subject = "subject";
	private final String message = "message";
	private final String htmlMessage = "htmlMessage";
	private final EmailBatchRequest.Sender sender = EmailBatchRequest.Sender.builder()
		.withName("name")
		.withAddress("address")
		.withReplyTo("replyTo")
		.build();
	private final EmailBatchRequest.Attachment attachment = EmailBatchRequest.Attachment.builder()
		.withName("name")
		.withContentType("contentType")
		.withContent("content")
		.build();
	private final EmailBatchRequest.Party party = EmailBatchRequest.Party.builder()
		.withPartyId("partyId")
		.build();
	private final Map<Header, List<String>> headers = Map.of(
		MESSAGE_ID, List.of("messageId"),
		REFERENCES, List.of("references"),
		IN_REPLY_TO, List.of("inReplyTo"));

	@Test
	void testBuilderMethods() {
		var emailBatchRequest = EmailBatchRequest.builder()
			.withOrigin(origin)
			.withSubject(subject)
			.withMessage(message)
			.withHtmlMessage(htmlMessage)
			.withHeaders(headers)
			.withSender(sender)
			.withAttachments(List.of(attachment))
			.withParties(List.of(party))
			.build();

		assertThat(emailBatchRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(emailBatchRequest.origin()).isEqualTo(origin);
		assertThat(emailBatchRequest.subject()).isEqualTo(subject);
		assertThat(emailBatchRequest.message()).isEqualTo(message);
		assertThat(emailBatchRequest.htmlMessage()).isEqualTo(htmlMessage);
		assertThat(emailBatchRequest.sender()).isEqualTo(sender);
		assertThat(emailBatchRequest.attachments()).containsExactly(attachment);
		assertThat(emailBatchRequest.parties()).containsExactly(party);
	}

	@Test
	void testConstructor() {
		var emailBatchRequest = new EmailBatchRequest(List.of(party), subject, message, htmlMessage, sender, origin, List.of(attachment), headers);

		assertThat(emailBatchRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(emailBatchRequest.origin()).isEqualTo(origin);
		assertThat(emailBatchRequest.subject()).isEqualTo(subject);
		assertThat(emailBatchRequest.message()).isEqualTo(message);
		assertThat(emailBatchRequest.htmlMessage()).isEqualTo(htmlMessage);
		assertThat(emailBatchRequest.sender()).isEqualTo(sender);
		assertThat(emailBatchRequest.attachments()).containsExactly(attachment);
		assertThat(emailBatchRequest.parties()).containsExactly(party);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(EmailBatchRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(new EmailBatchRequest(List.of(party), subject, message, htmlMessage, sender, origin, List.of(attachment), headers)).hasNoNullFieldsOrProperties();
	}
}

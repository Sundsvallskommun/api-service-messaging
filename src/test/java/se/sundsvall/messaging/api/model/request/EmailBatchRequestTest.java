package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.model.request.EmailBatchRequest.Attachment;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest.Party;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest.Sender;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class EmailBatchRequestTest {

	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String SUBJECT = "subject";
	private static final String MESSAGE = "message";
	private static final String HTML_MESSAGE = "htmlMessage";
	private static final String NAME = "name";
	private static final String ADDRESS = "address";
	private static final String REPLY_TO = "replyTo";
	private static final String PARTY_ID = "partyId";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final String ATTACHMENT_NAME = "attachmentName";
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "contentType";
	private static final EmailBatchRequest.Sender SENDER = EmailBatchRequest.Sender.builder().build();
	private static final List<EmailBatchRequest.Attachment> ATTACHMENTS = List.of(EmailBatchRequest.Attachment.builder().build());
	private static final List<EmailBatchRequest.Party> PARTIES = List.of(EmailBatchRequest.Party.builder().build());
	private static final Map<Header, List<String>> HEADERS = Map.of(MESSAGE_ID, List.of("value"));
	private static final String MUNICIPALITY_ID = "municipalityId";

	// EmailBatchRequest
	@Test
	void testEmailBatchRequestConstructor() {
		final var bean = new EmailBatchRequest(PARTIES, SUBJECT, MESSAGE, HTML_MESSAGE, SENDER, ORIGIN, ISSUER, ATTACHMENTS, HEADERS, MUNICIPALITY_ID);

		assertEmailBatchRequest(bean);
	}

	@Test
	void testEmailBatchRequestBuilder() {
		final var bean = EmailBatchRequest.builder()
			.withOrigin(ORIGIN)
			.withIssuer(ISSUER)
			.withSubject(SUBJECT)
			.withMessage(MESSAGE)
			.withHtmlMessage(HTML_MESSAGE)
			.withHeaders(HEADERS)
			.withSender(SENDER)
			.withAttachments(ATTACHMENTS)
			.withParties(PARTIES)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertEmailBatchRequest(bean);
	}

	private void assertEmailBatchRequest(final EmailBatchRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.htmlMessage()).isEqualTo(HTML_MESSAGE);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.parties()).isEqualTo(PARTIES);
		assertThat(bean.headers()).isEqualTo(HEADERS);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// EmailBatchRequest.Sender
	@Test
	void testEmailBatchRequestSenderConstructor() {
		final var bean = new EmailBatchRequest.Sender(NAME, ADDRESS, REPLY_TO);

		assertEmailBatchRequestSender(bean);
	}

	@Test
	void testEmailBatchRequestSenderBuilder() {
		final var bean = EmailBatchRequest.Sender.builder()
			.withAddress(ADDRESS)
			.withName(NAME)
			.withReplyTo(REPLY_TO)
			.build();

		assertEmailBatchRequestSender(bean);
	}

	private void assertEmailBatchRequestSender(final Sender bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean.name()).isEqualTo(NAME);
		assertThat(bean.replyTo()).isEqualTo(REPLY_TO);
	}

	// EmailBatchRequest.Party
	@Test
	void testEmailBatchRequestPartyConstructor() {
		final var bean = new EmailBatchRequest.Party(PARTY_ID, EMAIL_ADDRESS);

		assertEmailBatchRequestParty(bean);
	}

	@Test
	void testEmailBatchRequestPartyBuilder() {
		final var bean = EmailBatchRequest.Party.builder()
			.withEmailAddress(EMAIL_ADDRESS)
			.withPartyId(PARTY_ID)
			.build();

		assertEmailBatchRequestParty(bean);
	}

	private void assertEmailBatchRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.emailAddress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// EmailBatchRequest.Attachment
	@Test
	void testEmailBatchRequestAttachmentConstructor() {
		final var bean = new EmailBatchRequest.Attachment(ATTACHMENT_NAME, CONTENT_TYPE, CONTENT);

		assertEmailBatchRequestAttachment(bean);
	}

	@Test
	void testEmailBatchRequestAttachmentBuilder() {
		final var bean = EmailBatchRequest.Attachment.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withName(ATTACHMENT_NAME)
			.build();

		assertEmailBatchRequestAttachment(bean);
	}

	private void assertEmailBatchRequestAttachment(final Attachment bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.name()).isEqualTo(ATTACHMENT_NAME);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(EmailBatchRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailBatchRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailBatchRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailBatchRequest.Sender.builder().build()).hasAllNullFieldsOrProperties();
	}
}

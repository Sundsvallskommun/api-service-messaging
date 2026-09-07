package se.sundsvall.messaging.api.model.request;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.EmailRequest.Attachment;
import se.sundsvall.messaging.api.model.request.EmailRequest.Party;
import se.sundsvall.messaging.api.model.request.EmailRequest.Sender;
import se.sundsvall.messaging.model.ExternalReference;

import static org.assertj.core.api.Assertions.assertThat;

class EmailRequestTest {

	private static final Party PARTY = Party.builder().build();
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final List<String> RECIPIENTS = List.of("recipient1", "recipient2");
	private static final List<String> CC = List.of("cc1", "cc2");
	private static final String SUBJECT = "subject";
	private static final String MESSAGE = "message";
	private static final String HTML_MESSAGE = "htmlMessage";
	private static final Sender SENDER = Sender.builder().build();
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final List<Attachment> ATTACHMENTS = List.of(Attachment.builder().build());
	private static final Map<String, List<String>> HEADERS = Map.of(Header.REFERENCES.name(), List.of("value"));
	private static final String CONTENT = "content";
	private static final String CONTENT_TYPE = "contentType";
	private static final String OBJECT_ID = "f8e2bd3c-1a6b-4f5e-9d0a-2c7b1e4f6a58";
	private static final String NAME = "name";
	private static final String PARTY_ID = "partyId";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final String ADDRESS = "address";
	private static final String REPLY_TO = "replyTo";
	private static final String MUNICIPALITY_ID = "municipalityId";

	// EmailRequest
	@Test
	void testEmailRequestConstructor() {
		final var bean = new EmailRequest(PARTY, EMAIL_ADDRESS, RECIPIENTS, CC, SUBJECT, MESSAGE, HTML_MESSAGE, SENDER, ORIGIN, ISSUER, ATTACHMENTS, HEADERS, MUNICIPALITY_ID);

		assertEmailRequest(bean);
	}

	@Test
	void testEmailRequestBuilder() {
		final var bean = EmailRequest.builder()
			.withAttachments(ATTACHMENTS)
			.withEmailAddress(EMAIL_ADDRESS)
			.withRecipients(RECIPIENTS)
			.withCc(CC)
			.withHeaders(HEADERS)
			.withHtmlMessage(HTML_MESSAGE)
			.withIssuer(ISSUER)
			.withMessage(MESSAGE)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withSender(SENDER)
			.withSubject(SUBJECT)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertEmailRequest(bean);
	}

	private void assertEmailRequest(final EmailRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.emailAddress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(bean.recipients()).isEqualTo(RECIPIENTS);
		assertThat(bean.cc()).isEqualTo(CC);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.htmlMessage()).isEqualTo(HTML_MESSAGE);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.headers()).isEqualTo(HEADERS);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// EmailRequest.Attachment
	@Test
	void testEmailRequestAttachmentConstructor() {
		final var bean = new EmailRequest.Attachment(NAME, CONTENT_TYPE, CONTENT, null);

		assertEmailRequestAttachment(bean);
	}

	@Test
	void testEmailRequestAttachmentBuilder() {
		final var bean = EmailRequest.Attachment.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withName(NAME)
			.build();

		assertEmailRequestAttachment(bean);
	}

	@Test
	void testEmailRequestAttachmentAsObjectReference() {
		final var bean = EmailRequest.Attachment.builder()
			.withName(NAME)
			.withContentType(CONTENT_TYPE)
			.withObjectId(OBJECT_ID)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("content");
		assertThat(bean.objectId()).isEqualTo(OBJECT_ID);
		assertThat(bean.content()).isNull();
	}

	private void assertEmailRequestAttachment(final Attachment bean) {
		// objectId is excepted rather than populated: it and content are mutually exclusive, so a bean carrying both
		// would be one the API rejects, and asserting on it would be asserting on a shape that cannot occur.
		assertThat(bean).isNotNull().hasNoNullFieldsOrPropertiesExcept("objectId");
		assertThat(bean.name()).isEqualTo(NAME);
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
	}

	// EmailRequest.Party
	@Test
	void testEmailRequestPartyConstructor() {
		final var bean = new EmailRequest.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertEmailRequestParty(bean);
	}

	@Test
	void testEmailRequestPartyBuilder() {
		final var bean = EmailRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertEmailRequestParty(bean);
	}

	private void assertEmailRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
	}

	// EmailRequest.Sender
	@Test
	void testEmailRequestSenderConstructor() {
		final var bean = new EmailRequest.Sender(NAME, ADDRESS, REPLY_TO);

		assertEmailRequestSender(bean);
	}

	@Test
	void testEmailRequestSenderBuilder() {
		final var bean = EmailRequest.Sender.builder()
			.withAddress(ADDRESS)
			.withName(NAME)
			.withReplyTo(REPLY_TO)
			.build();

		assertEmailRequestSender(bean);
	}

	private void assertEmailRequestSender(final Sender bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.address()).isEqualTo(ADDRESS);
		assertThat(bean.name()).isEqualTo(NAME);
		assertThat(bean.replyTo()).isEqualTo(REPLY_TO);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(EmailRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(EmailRequest.Sender.builder().build()).hasAllNullFieldsOrProperties();
	}
}

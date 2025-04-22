package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Attachment;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Party;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class WebMessageRequestTest {

	private static final Party PARTY = Party.builder().build();
	private static final String MESSAGE = "message";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final String OEP_INSTANCE = "oepInstance";
	private static final List<Attachment> ATTACHMENTS = List.of(Attachment.builder().build());
	private static final String FILE_NAME = "fileName";
	private static final String MIME_TYPE = "mimeType";
	private static final String BASE64_DATA = "base64Data";
	private static final String PARTY_ID = "partyId";
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String USER_ID = "userId";
	private static final Boolean SEND_AS_OWNER = false;
	private static final WebMessageRequest.Sender SENDER = WebMessageRequest.Sender.builder().build();

	// WebMessageRequest
	@Test
	void testWebMessageRequestConstructor() {
		final var bean = new WebMessageRequest(PARTY, MESSAGE, SENDER, SEND_AS_OWNER, ORIGIN, ISSUER, OEP_INSTANCE, ATTACHMENTS, MUNICIPALITY_ID);

		assertWebMessageRequest(bean);
	}

	@Test
	void testWebMessageRequestBuilder() {
		final var bean = WebMessageRequest.builder()
			.withAttachments(ATTACHMENTS)
			.withIssuer(ISSUER)
			.withMessage(MESSAGE)
			.withSender(SENDER)
			.withOepInstance(OEP_INSTANCE)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertWebMessageRequest(bean);
	}

	private void assertWebMessageRequest(final WebMessageRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.oepInstance()).isEqualTo(OEP_INSTANCE);
		assertThat(bean.message()).isEqualTo(MESSAGE);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// WebMessageRequest.Attachment
	@Test
	void testWebMessageRequestAttachmentConstructor() {
		final var bean = new WebMessageRequest.Attachment(FILE_NAME, MIME_TYPE, BASE64_DATA);

		assertWebMessageRequestAttachment(bean);
	}

	@Test
	void testWebMessageRequestAttachmentBuilder() {
		final var bean = WebMessageRequest.Attachment.builder()
			.withBase64Data(BASE64_DATA)
			.withFileName(FILE_NAME)
			.withMimeType(MIME_TYPE)
			.build();

		assertWebMessageRequestAttachment(bean);
	}

	private void assertWebMessageRequestAttachment(final Attachment bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.base64Data()).isEqualTo(BASE64_DATA);
		assertThat(bean.fileName()).isEqualTo(FILE_NAME);
		assertThat(bean.mimeType()).isEqualTo(MIME_TYPE);
	}

	// WebMessageRequest.Party
	@Test
	void testWebMessageRequestPartyConstructor() {
		final var bean = new WebMessageRequest.Party(PARTY_ID, EXTERNAL_REFERENCES);

		assertWebMessageRequestParty(bean);
	}

	@Test
	void testWebMessageRequestPartyBuilder() {
		final var bean = WebMessageRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyId(PARTY_ID)
			.build();

		assertWebMessageRequestParty(bean);
	}

	private void assertWebMessageRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyId()).isEqualTo(PARTY_ID);
	}

	// WebMessageRequest.Sender
	@Test
	void testWebMessageRequestSenderConstructor() {
		final var bean = new WebMessageRequest.Sender(USER_ID);

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.userId()).isEqualTo(USER_ID);
	}

	@Test
	void testWebMessageRequestSenderBuilder() {
		final var bean = WebMessageRequest.Sender.builder()
			.withUserId(USER_ID)
			.build();

		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.userId()).isEqualTo(USER_ID);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(WebMessageRequest.builder().build()).hasAllNullFieldsOrPropertiesExcept("sendAsOwner");
		assertThat(WebMessageRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(WebMessageRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(WebMessageRequest.Sender.builder().build()).hasAllNullFieldsOrProperties();
	}
}

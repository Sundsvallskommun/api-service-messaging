package se.sundsvall.messaging.api.model.request;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest.Attachment;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest.Party;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest.Sender;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest.Sender.SupportInfo;
import se.sundsvall.messaging.model.ExternalReference;

import static org.assertj.core.api.Assertions.assertThat;

class DigitalMailRequestTest {

	private static final String SUBJECT = "subject";
	private static final String DEPARTMENT = "department";
	private static final String CONTENT_TYPE = "contentType";
	private static final String BODY = "body";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final DigitalMailRequest.Party PARTY = DigitalMailRequest.Party.builder().build();
	private static final DigitalMailRequest.Sender SENDER = DigitalMailRequest.Sender.builder().build();
	private static final List<DigitalMailRequest.Attachment> ATTACHMENTS = List.of(DigitalMailRequest.Attachment.builder().build());
	private static final List<String> PARTY_IDS = List.of("partyId");
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final DigitalMailRequest.Sender.SupportInfo SUPPORT_INFO = DigitalMailRequest.Sender.SupportInfo.builder().build();
	private static final String TEXT = "text";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final String PHONE_NUMBER = "phonenumber";
	private static final String URL = "url";
	private static final String CONTENT = "content";
	private static final String FILENAME = "someFilename";
	private static final String MUNICIPALITY_ID = "municipalityId";

	// DigitalMailRequest
	@Test
	void testDigitalMailRequestConstructor() {
		final var bean = new DigitalMailRequest(PARTY, SENDER, SUBJECT, DEPARTMENT, CONTENT_TYPE, BODY, ORIGIN, ISSUER, ATTACHMENTS, MUNICIPALITY_ID);

		assertDigitalMailRequest(bean);
	}

	@Test
	void testDigitalMailRequestBuilder() {
		final var bean = DigitalMailRequest.builder()
			.withAttachments(ATTACHMENTS)
			.withBody(BODY)
			.withContentType(CONTENT_TYPE)
			.withDepartment(DEPARTMENT)
			.withIssuer(ISSUER)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withSender(SENDER)
			.withSubject(SUBJECT)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertDigitalMailRequest(bean);
	}

	private void assertDigitalMailRequest(final DigitalMailRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.department()).isEqualTo(DEPARTMENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.body()).isEqualTo(BODY);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// DigitalMailRequest.Party
	@Test
	void testDigitalMailRequestPartyConstructor() {
		final var bean = new DigitalMailRequest.Party(PARTY_IDS, EXTERNAL_REFERENCES);

		assertDigitalMailRequestParty(bean);
	}

	@Test
	void testDigitalMailRequestPartyBuilder() {
		final var bean = DigitalMailRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyIds(PARTY_IDS)
			.build();

		assertDigitalMailRequestParty(bean);
	}

	private void assertDigitalMailRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.partyIds()).isEqualTo(PARTY_IDS);
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
	}

	// DigitalMailRequest.Sender
	@Test
	void testDigitalMailRequestSenderConstructor() {
		final var bean = new DigitalMailRequest.Sender(SUPPORT_INFO);

		assertDigitalMailRequestSender(bean);
	}

	@Test
	void testDigitalMailRequestSenderBuilder() {
		final var bean = DigitalMailRequest.Sender.builder()
			.withSupportInfo(SUPPORT_INFO)
			.build();

		assertDigitalMailRequestSender(bean);
	}

	private void assertDigitalMailRequestSender(final Sender bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.supportInfo()).isEqualTo(SUPPORT_INFO);
	}

	// DigitalMailRequest.Sender.SupportInfo
	@Test
	void testDigitalMailRequestSenderSupportInfoConstructor() {
		final var bean = new DigitalMailRequest.Sender.SupportInfo(TEXT, EMAIL_ADDRESS, PHONE_NUMBER, URL);

		assertDigitalMailRequestSenderSupportInfo(bean);
	}

	@Test
	void testDigitalMailRequestSenderSupportInfoBuilder() {
		final var bean = DigitalMailRequest.Sender.SupportInfo.builder()
			.withEmailAddress(EMAIL_ADDRESS)
			.withPhoneNumber(PHONE_NUMBER)
			.withText(TEXT)
			.withUrl(URL)
			.build();

		assertDigitalMailRequestSenderSupportInfo(bean);
	}

	private void assertDigitalMailRequestSenderSupportInfo(final SupportInfo bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.text()).isEqualTo(TEXT);
		assertThat(bean.emailAddress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(bean.phoneNumber()).isEqualTo(PHONE_NUMBER);
		assertThat(bean.url()).isEqualTo(URL);
	}

	// DigitalMailRequest.Attachment
	@Test
	void testDigitalMailRequestAttachmentConstructor() {
		final var bean = new DigitalMailRequest.Attachment(CONTENT_TYPE, CONTENT, FILENAME);

		assertDigitalMailRequestAttachment(bean);
	}

	@Test
	void testDigitalMailRequestAttachmentBuilder() {
		final var bean = DigitalMailRequest.Attachment.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withFilename(FILENAME)
			.build();

		assertDigitalMailRequestAttachment(bean);
	}

	private void assertDigitalMailRequestAttachment(final Attachment bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.filename()).isEqualTo(FILENAME);
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(DigitalMailRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalMailRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalMailRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalMailRequest.Sender.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(DigitalMailRequest.Sender.SupportInfo.builder().build()).hasAllNullFieldsOrProperties();
	}
}

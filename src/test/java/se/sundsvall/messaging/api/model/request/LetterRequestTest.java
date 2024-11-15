package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createAddress;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.ANY;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.DIGITAL_MAIL;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.SNAIL_MAIL;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.LetterRequest.Attachment;
import se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode;
import se.sundsvall.messaging.api.model.request.LetterRequest.Party;
import se.sundsvall.messaging.api.model.request.LetterRequest.Sender;
import se.sundsvall.messaging.api.model.request.LetterRequest.Sender.SupportInfo;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class LetterRequestTest {

	private static final Party PARTY = Party.builder().build();
	private static final String SUBJECT = "subject";
	private static final Sender SENDER = Sender.builder().build();
	private static final String CONTENT_TYPE = "contentType";
	private static final String BODY = "body";
	private static final String DEPARTMENT = "department";
	private static final String DEVIATION = "deviation";
	private static final String ORIGIN = "origin";
	private static final String ISSUER = "issuer";
	private static final List<Attachment> ATTACHMENTS = List.of(Attachment.builder().build());
	private static final DeliveryMode DELIVERY_MODE = DIGITAL_MAIL;
	private static final String FILENAME = "filename";
	private static final String CONTENT = "content";
	private static final List<String> PARTY_IDS = List.of("partyId");
	private static final List<ExternalReference> EXTERNAL_REFERENCES = List.of(ExternalReference.builder().build());
	private static final SupportInfo SUPPORT_INFO = SupportInfo.builder().build();
	private static final String TEXT = "text";
	private static final String EMAIL_ADDRESS = "emailAddress";
	private static final String PHONE_NUMBER = "phonenumber";
	private static final String URL = "url";
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final Address ADDRESS = createAddress();

	// LetterRequest
	@Test
	void testLetterRequestConstructor() {
		final var bean = new LetterRequest(PARTY, SUBJECT, SENDER, CONTENT_TYPE, BODY, DEPARTMENT, DEVIATION, ORIGIN, ISSUER, ATTACHMENTS, MUNICIPALITY_ID);

		assertLetterRequest(bean);
	}

	@Test
	void testLetterRequestBuilder() {
		final var bean = LetterRequest.builder()
			.withAttachments(ATTACHMENTS)
			.withBody(BODY)
			.withContentType(CONTENT_TYPE)
			.withDepartment(DEPARTMENT)
			.withDeviation(DEVIATION)
			.withIssuer(ISSUER)
			.withOrigin(ORIGIN)
			.withParty(PARTY)
			.withSender(SENDER)
			.withSubject(SUBJECT)
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();

		assertLetterRequest(bean);
	}

	private void assertLetterRequest(final LetterRequest bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.party()).isEqualTo(PARTY);
		assertThat(bean.sender()).isEqualTo(SENDER);
		assertThat(bean.subject()).isEqualTo(SUBJECT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.body()).isEqualTo(BODY);
		assertThat(bean.department()).isEqualTo(DEPARTMENT);
		assertThat(bean.deviation()).isEqualTo(DEVIATION);
		assertThat(bean.attachments()).isEqualTo(ATTACHMENTS);
		assertThat(bean.origin()).isEqualTo(ORIGIN);
		assertThat(bean.issuer()).isEqualTo(ISSUER);
		assertThat(bean.municipalityId()).isEqualTo(MUNICIPALITY_ID);
	}

	// LetterRequest.Attachment
	@Test
	void testLetterRequestAttachmentConstructor() {
		final var bean = new LetterRequest.Attachment(DELIVERY_MODE, FILENAME, CONTENT_TYPE, CONTENT);

		assertLetterRequestAttachment(bean);
	}

	@Test
	void testLetterRequestAttachmentBuilder() {
		final var bean = LetterRequest.Attachment.builder()
			.withContent(CONTENT)
			.withContentType(CONTENT_TYPE)
			.withDeliveryMode(DELIVERY_MODE)
			.withFilename(FILENAME)
			.build();

		assertLetterRequestAttachment(bean);
	}

	private void assertLetterRequestAttachment(final Attachment bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.content()).isEqualTo(CONTENT);
		assertThat(bean.contentType()).isEqualTo(CONTENT_TYPE);
		assertThat(bean.deliveryMode()).isEqualTo(DELIVERY_MODE);
		assertThat(bean.filename()).isEqualTo(FILENAME);
	}

	// LetterRequest.Party
	@Test
	void testLetterRequestPartyConstructor() {
		final var bean = new LetterRequest.Party(PARTY_IDS, List.of(ADDRESS), EXTERNAL_REFERENCES);

		assertLetterRequestParty(bean);
	}

	@Test
	void testLetterRequestPartyBuilder() {
		final var bean = LetterRequest.Party.builder()
			.withExternalReferences(EXTERNAL_REFERENCES)
			.withPartyIds(PARTY_IDS)
			.withAddresses(List.of(ADDRESS))
			.build();

		assertLetterRequestParty(bean);
	}

	private void assertLetterRequestParty(final Party bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.externalReferences()).isEqualTo(EXTERNAL_REFERENCES);
		assertThat(bean.partyIds()).isEqualTo(PARTY_IDS);
		assertThat(bean.addresses()).containsExactly(ADDRESS);
	}

	// LetterRequest.Sender
	@Test
	void testLetterRequestSenderConstructor() {
		final var bean = new LetterRequest.Sender(SUPPORT_INFO);

		assertLetterRequestSender(bean);
	}

	@Test
	void testLetterRequestSenderBuilder() {
		final var bean = LetterRequest.Sender.builder()
			.withSupportInfo(SUPPORT_INFO)
			.build();

		assertLetterRequestSender(bean);
	}

	private void assertLetterRequestSender(final Sender bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.supportInfo()).isEqualTo(SUPPORT_INFO);
	}

	// LetterRequest.Sender.SupportInfo
	@Test
	void testLetterRequestSenderSupportInfoConstructor() {
		final var bean = new LetterRequest.Sender.SupportInfo(TEXT, EMAIL_ADDRESS, PHONE_NUMBER, URL);

		assertLetterRequestSenderSupportInfo(bean);
	}

	@Test
	void testLetterRequestSenderSupportInfoBuilder() {
		final var bean = LetterRequest.Sender.SupportInfo.builder()
			.withEmailAddress(EMAIL_ADDRESS)
			.withPhoneNumber(PHONE_NUMBER)
			.withText(TEXT)
			.withUrl(URL)
			.build();

		assertLetterRequestSenderSupportInfo(bean);
	}

	private void assertLetterRequestSenderSupportInfo(final SupportInfo bean) {
		assertThat(bean).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(bean.emailAddress()).isEqualTo(EMAIL_ADDRESS);
		assertThat(bean.phoneNumber()).isEqualTo(PHONE_NUMBER);
		assertThat(bean.text()).isEqualTo(TEXT);
		assertThat(bean.url()).isEqualTo(URL);
	}

	// Other
	@Test
	void testEnumValidValues() {
		assertThat(LetterRequest.Attachment.DeliveryMode.values()).containsExactlyInAnyOrder(ANY, DIGITAL_MAIL, SNAIL_MAIL);
	}

	@Test
	void testAttachmentDeliveryIntendedMethods() {
		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(ANY).build().isIntendedForDigitalMail()).isTrue();
		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(ANY).build().isIntendedForSnailMail()).isTrue();

		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(SNAIL_MAIL).build().isIntendedForDigitalMail()).isFalse();
		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(SNAIL_MAIL).build().isIntendedForSnailMail()).isTrue();

		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(DIGITAL_MAIL).build().isIntendedForDigitalMail()).isTrue();
		assertThat(LetterRequest.Attachment.builder().withDeliveryMode(DIGITAL_MAIL).build().isIntendedForSnailMail()).isFalse();
	}

	// No dirt
	@Test
	void testNoDirtOnCreatedBeans() {
		assertThat(LetterRequest.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(LetterRequest.Attachment.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(LetterRequest.Party.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(LetterRequest.Sender.builder().build()).hasAllNullFieldsOrProperties();
		assertThat(LetterRequest.Sender.SupportInfo.builder().build()).hasAllNullFieldsOrProperties();
	}
}

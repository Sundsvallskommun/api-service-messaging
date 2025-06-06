package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestAttachment;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestParty;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequestSender;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.ExternalReference;

class RequestMapperTest {

	private final RequestMapper mapper;

	RequestMapperTest() {
		mapper = new RequestMapper(
			new Defaults(
				new Defaults.Sms("name"),
				new Defaults.Email("name", "address", "replyTo"),
				new Defaults.DigitalMail("municipalityId",
					new Defaults.DigitalMail.SupportInfo("text", "emailAddress", "phoneNumber", "url"), "subject")));
	}

	@Test
	void toDigitalMailRequest() {

		// Arrange
		final var body = "body";
		final var content = "content";
		final var contentType = "contentType";
		final var department = "department";
		final var deviation = "deviation";
		final var emailAddress = "emailAddress";
		final var filename = "filename";
		final var key = "key";
		final var origin = "origin";
		final var partyId = "partyId";
		final var phoneNumber = "phoneNumber";
		final var subject = "subject";
		final var text = "text";
		final var url = "url";
		final var value = "value";
		final var attachments = createAttachments(content, contentType, filename);
		final var party = LetterRequest.Party.builder()
			.withExternalReferences(List.of(ExternalReference.builder()
				.withKey(key)
				.withValue(value)
				.build()))
			.build();
		final var supportInfo = LetterRequest.Sender.SupportInfo.builder()
			.withEmailAddress(emailAddress)
			.withPhoneNumber(phoneNumber)
			.withText(text)
			.withUrl(url)
			.build();
		final var sender = LetterRequest.Sender.builder()
			.withSupportInfo(supportInfo)
			.build();
		final var letterRequest = LetterRequest.builder()
			.withAttachments(attachments)
			.withBody(body)
			.withContentType(contentType)
			.withDepartment(department)
			.withDeviation(deviation)
			.withOrigin(origin)
			.withParty(party)
			.withSubject(subject)
			.withSender(sender)
			.build();

		// Act
		final var digitalMailRequest = mapper.toDigitalMailRequest(letterRequest, partyId);

		// Assert
		assertThat(digitalMailRequest.attachments()).hasSize(2).satisfiesExactlyInAnyOrder(attachment -> {
			assertThat(attachment.content()).isEqualTo(content + DeliveryMode.ANY);
			assertThat(attachment.contentType()).isEqualTo(contentType + DeliveryMode.ANY);
			assertThat(attachment.filename()).isEqualTo(filename + DeliveryMode.ANY);
		}, attachment -> {
			assertThat(attachment.content()).isEqualTo(content + DeliveryMode.DIGITAL_MAIL);
			assertThat(attachment.contentType()).isEqualTo(contentType + DeliveryMode.DIGITAL_MAIL);
			assertThat(attachment.filename()).isEqualTo(filename + DeliveryMode.DIGITAL_MAIL);
		});

		assertThat(digitalMailRequest.body()).isEqualTo(body);
		assertThat(digitalMailRequest.contentType()).isEqualTo(contentType);
		assertThat(digitalMailRequest.department()).isEqualTo(department);
		assertThat(digitalMailRequest.origin()).isEqualTo(origin);
		assertThat(digitalMailRequest.party()).isNotNull().satisfies(digitalMailRequestParty -> {
			assertThat(digitalMailRequestParty.partyIds()).isNotNull().containsExactly(partyId);
			assertThat(digitalMailRequestParty.externalReferences()).isNotNull().satisfiesExactly(externalRef -> {
				assertThat(externalRef.key()).isEqualTo(key);
				assertThat(externalRef.value()).isEqualTo(value);
			});
		});
		assertThat(digitalMailRequest.sender()).isNotNull().satisfies(digitalMailRequestSender -> {
			assertThat(digitalMailRequestSender.supportInfo()).isNotNull().satisfies(digitalMailRequestSupportInfo -> {
				assertThat(digitalMailRequestSupportInfo.emailAddress()).isEqualTo(emailAddress);
				assertThat(digitalMailRequestSupportInfo.phoneNumber()).isEqualTo(phoneNumber);
				assertThat(digitalMailRequestSupportInfo.text()).isEqualTo(text);
				assertThat(digitalMailRequestSupportInfo.url()).isEqualTo(url);
			});
		});

		assertThat(digitalMailRequest.subject()).isEqualTo(subject);
	}

	@Test
	void toSnailMailRequest() {
		// Arrange
		final var body = "body";
		final var content = "content";
		final var contentType = "contentType";
		final var department = "department";
		final var deviation = "deviation";
		final var filename = "filename";
		final var origin = "origin";
		final var partyId = "partyId";
		final var subject = "subject";
		final var attachments = createAttachments(content, contentType, filename);
		final var address = Address.builder()
			.withAddress("someAddress")
			.withCountry("someCountry")
			.withZipCode("someZipCode")
			.withFirstName("someFirstName")
			.withLastName("someLastName")
			.withCity("someCity")
			.withApartmentNumber("someApartmentNumber")
			.build();
		final var letterRequest = LetterRequest.builder()
			.withAttachments(attachments)
			.withBody(body)
			.withContentType(contentType)
			.withDepartment(department)
			.withDeviation(deviation)
			.withOrigin(origin)
			.withSubject(subject)
			.build();

		// Act
		final var snailMailRequest = mapper.toSnailMailRequest(letterRequest, partyId, address);

		// Assert
		assertThat(snailMailRequest.attachments()).hasSize(2).satisfiesExactlyInAnyOrder(attachment -> {
			assertThat(attachment.content()).isEqualTo(content + DeliveryMode.ANY);
			assertThat(attachment.contentType()).isEqualTo(contentType + DeliveryMode.ANY);
			assertThat(attachment.filename()).isEqualTo(filename + DeliveryMode.ANY);
		}, attachment -> {
			assertThat(attachment.content()).isEqualTo(content + DeliveryMode.SNAIL_MAIL);
			assertThat(attachment.contentType()).isEqualTo(contentType + DeliveryMode.SNAIL_MAIL);
			assertThat(attachment.filename()).isEqualTo(filename + DeliveryMode.SNAIL_MAIL);
		});

		assertThat(snailMailRequest.address()).isEqualTo(address);
		assertThat(snailMailRequest.department()).isEqualTo(department);
		assertThat(snailMailRequest.deviation()).isEqualTo(deviation);
		assertThat(snailMailRequest.origin()).isEqualTo(origin);
		assertThat(snailMailRequest.party()).isNotNull().satisfies(party -> {
			assertThat(party.externalReferences()).isNullOrEmpty();
			assertThat(party.partyId()).isEqualTo(partyId);
		});
	}

	@Test
	void toSmsRequestFromSmsBatchRequest() {
		// Arrange
		final var message = "message";
		final var mobileNumber = "mobileNumber";
		final var origin = "origin";
		final var sender = "sender";
		final var partyId = "partyId";
		final var party = SmsBatchRequest.Party.builder()
			.withMobileNumber(mobileNumber)
			.withPartyId(partyId)
			.build();
		final var smsBatchRequest = SmsBatchRequest.builder()
			.withMessage(message)
			.withOrigin(origin)
			.withParties(List.of(party))
			.withSender(sender)
			.withPriority(Priority.HIGH)
			.build();

		// Act
		final var smsRequest = mapper.toSmsRequest(smsBatchRequest, party);

		// Assert
		assertThat(smsRequest.message()).isEqualTo(message);
		assertThat(smsRequest.mobileNumber()).isEqualTo(mobileNumber);
		assertThat(smsRequest.origin()).isEqualTo(origin);
		assertThat(smsRequest.sender()).isEqualTo(sender);
		assertThat(smsRequest.party()).isNotNull().satisfies(smsParty -> {
			assertThat(smsParty.externalReferences()).isNullOrEmpty();
			assertThat(smsParty.partyId()).isEqualTo(partyId);
		});
		assertThat(smsRequest.priority()).isEqualTo(Priority.HIGH);
	}

	private List<LetterRequest.Attachment> createAttachments(String content, String contentType, String filename) {
		return List.of(
			LetterRequest.Attachment.builder()
				.withContent(content + DeliveryMode.ANY)
				.withContentType(contentType + DeliveryMode.ANY)
				.withFilename(filename + DeliveryMode.ANY)
				.withDeliveryMode(DeliveryMode.ANY)
				.build(),
			LetterRequest.Attachment.builder()
				.withContent(content + DeliveryMode.SNAIL_MAIL)
				.withContentType(contentType + DeliveryMode.SNAIL_MAIL)
				.withFilename(filename + DeliveryMode.SNAIL_MAIL)
				.withDeliveryMode(DeliveryMode.SNAIL_MAIL)
				.build(),
			LetterRequest.Attachment.builder()
				.withContent(content + DeliveryMode.DIGITAL_MAIL)
				.withContentType(contentType + DeliveryMode.DIGITAL_MAIL)
				.withFilename(filename + DeliveryMode.DIGITAL_MAIL)
				.withDeliveryMode(DeliveryMode.DIGITAL_MAIL)
				.build());
	}

	@Test
	void toEmailRequest() {
		final var emailBatchRequest = createValidEmailBatchRequest();
		final var party = createValidEmailBatchRequestParty();

		final var emailRequest = mapper.toEmailRequest(emailBatchRequest, party);

		assertThat(emailRequest.emailAddress()).isEqualTo(party.emailAddress());
		assertThat(emailRequest.headers()).isEqualTo(emailBatchRequest.headers());
		assertThat(emailRequest.message()).isEqualTo(emailBatchRequest.message());
		assertThat(emailRequest.subject()).isEqualTo(emailBatchRequest.subject());
		assertThat(emailRequest.origin()).isEqualTo(emailBatchRequest.origin());
		assertThat(emailRequest.htmlMessage()).isEqualTo(emailBatchRequest.htmlMessage());

		assertThat(emailRequest.sender()).satisfies(s -> {
			assertThat(s.address()).isEqualTo(emailBatchRequest.sender().address());
			assertThat(s.name()).isEqualTo(emailBatchRequest.sender().name());
			assertThat(s.replyTo()).isEqualTo(emailBatchRequest.sender().replyTo());
		});

		assertThat(emailRequest.attachments()).extracting("contentType", "content", "name")
			.containsExactlyInAnyOrder(tuple("text/plain", "c29tZUJhc2U2NENvbnRlbnQ=", "someName"));

		// Verify that all party-fields are set correctly
		assertThat(emailRequest.party()).satisfies(p -> assertThat(p.partyId()).isEqualTo(party.partyId()));
	}

	@Test
	void toEmailRequestSender() {
		final var sender = createValidEmailBatchRequestSender();

		final var emailRequestSender = mapper.toEmailRequestSender(sender);

		assertThat(emailRequestSender).isNotNull();
		assertThat(emailRequestSender.address()).isEqualTo(sender.address());
		assertThat(emailRequestSender.name()).isEqualTo(sender.name());
		assertThat(emailRequestSender.replyTo()).isEqualTo(sender.replyTo());
	}

	@Test
	void toEmailRequestParty() {
		final var party = createValidEmailBatchRequestParty();

		final var emailRequestParty = mapper.toEmailRequestParty(party);

		assertThat(emailRequestParty).isNotNull();
		assertThat(emailRequestParty.partyId()).isEqualTo(party.partyId());
	}

	@Test
	void toEmailRequestAttachment() {
		final var attachment = createValidEmailBatchRequestAttachment();

		final var emailRequestAttachment = mapper.toEmailRequestAttachment(attachment);

		assertThat(emailRequestAttachment).isNotNull();
		assertThat(emailRequestAttachment.contentType()).isEqualTo(attachment.contentType());
		assertThat(emailRequestAttachment.content()).isEqualTo(attachment.content());
		assertThat(emailRequestAttachment.name()).isEqualTo(attachment.name());
	}
}

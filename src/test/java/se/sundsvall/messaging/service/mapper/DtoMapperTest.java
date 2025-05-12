package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static se.sundsvall.messaging.Constants.OEP_INSTANCE_EXTERNAL;
import static se.sundsvall.messaging.Constants.OEP_INSTANCE_INTERNAL;
import static se.sundsvall.messaging.TestDataFactory.createAddress;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalInvoiceRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;

import java.util.UUID;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalInvoiceDto;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailDto;
import se.sundsvall.messaging.integration.oepintegrator.WebMessageDto;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailDto;
import se.sundsvall.messaging.model.ContentType;

class DtoMapperTest {

	private DtoMapper dtoMapper;

	@BeforeEach
	void setUp() {
		dtoMapper = new DtoMapper(
			new Defaults(
				new Defaults.Sms("name"),
				new Defaults.Email("name", "address", "replyTo"),
				new Defaults.DigitalMail("municipalityId",
					new Defaults.DigitalMail.SupportInfo("text", "emailAddress", "phoneNumber", "url"), "subject")));
	}

	@Test
	void toEmailDtoTest() {
		final var emailRequest = createValidEmailRequest();

		final var emailDto = dtoMapper.toEmailDto(emailRequest);

		assertThat(emailDto.emailAddress()).isEqualTo(emailRequest.emailAddress());
		assertThat(emailDto.sender().name()).isEqualTo(emailRequest.sender().name());
		assertThat(emailDto.sender().address()).isEqualTo(emailRequest.sender().address());
		assertThat(emailDto.sender().replyTo()).isEqualTo(emailRequest.sender().replyTo());
		assertThat(emailDto.subject()).isEqualTo(emailRequest.subject());
		assertThat(emailDto.message()).isEqualTo(emailRequest.message());
		assertThat(emailDto.attachments()).hasSize(emailRequest.attachments().size());
		assertThat(emailDto.headers().get("MESSAGE_ID")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(MESSAGE_ID.name()));
		assertThat(emailDto.headers().get("IN_REPLY_TO")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(IN_REPLY_TO.name()));
		assertThat(emailDto.headers().get("REFERENCES")).containsExactlyInAnyOrderElementsOf(emailRequest.headers().get(REFERENCES.name()));

	}

	@Test
	void toSmsDtoTest() {
		final var smsRequest = createValidSmsRequest();

		final var smsDto = dtoMapper.toSmsDto(smsRequest);

		assertThat(smsDto.message()).isEqualTo(smsRequest.message());
		assertThat(smsDto.mobileNumber()).isEqualTo(smsRequest.mobileNumber());
		assertThat(smsDto.priority()).isEqualTo(smsRequest.priority());
		assertThat(smsDto.sender()).isEqualTo(smsRequest.sender());
	}

	@Test
	void toSmsDtoWithoutSetPriorityTest() {
		final var smsRequest = createValidSmsRequest()
			.withPriority(null);

		final var smsDto = dtoMapper.toSmsDto(smsRequest);

		assertThat(smsDto.message()).isEqualTo(smsRequest.message());
		assertThat(smsDto.mobileNumber()).isEqualTo(smsRequest.mobileNumber());
		assertThat(smsDto.priority()).isEqualTo(Priority.NORMAL);
		assertThat(smsDto.sender()).isEqualTo(smsRequest.sender());
	}

	@Test
	void toDigitalMailDtoTest() {
		final var digitalMailRequest = createValidDigitalMailRequest();
		final var partyId = UUID.randomUUID().toString();
		final var digitalMailDto = dtoMapper.toDigitalMailDto(digitalMailRequest, partyId);

		assertThat(digitalMailDto.sender().municipalityId()).isEqualTo("municipalityId");
		assertThat(digitalMailDto.sender().supportInfo().text()).isEqualTo(digitalMailRequest.sender().supportInfo().text());
		assertThat(digitalMailDto.sender().supportInfo().emailAddress()).isEqualTo(digitalMailRequest.sender().supportInfo().emailAddress());
		assertThat(digitalMailDto.sender().supportInfo().phoneNumber()).isEqualTo(digitalMailRequest.sender().supportInfo().phoneNumber());
		assertThat(digitalMailDto.sender().supportInfo().url()).isEqualTo(digitalMailRequest.sender().supportInfo().url());
		assertThat(digitalMailDto.partyId()).isEqualTo(partyId);
		assertThat(digitalMailDto.contentType()).isEqualTo(ContentType.fromString(digitalMailRequest.contentType()));
		assertThat(digitalMailDto.subject()).isEqualTo(digitalMailRequest.subject());
		assertThat(digitalMailDto.body()).isEqualTo(digitalMailRequest.body());

		assertThat(digitalMailDto.attachments())
			.extracting(DigitalMailDto.Attachment::filename, DigitalMailDto.Attachment::contentType, DigitalMailDto.Attachment::content)
			.containsExactlyInAnyOrder(
				digitalMailRequest.attachments().stream()
					.map(attachment -> tuple(attachment.filename(), ContentType.fromString(attachment.contentType()), attachment.content()))
					.toArray(Tuple[]::new));
	}

	@Test
	void toDigitalInvoiceDtoTest() {
		final var digitalInvoiceRequest = createValidDigitalInvoiceRequest();
		final var digitalInvoiceDto = dtoMapper.toDigitalInvoiceDto(digitalInvoiceRequest);

		assertThat(digitalInvoiceDto.partyId()).isEqualTo(digitalInvoiceRequest.party().partyId());
		assertThat(digitalInvoiceDto.type()).isEqualTo(digitalInvoiceRequest.type());
		assertThat(digitalInvoiceDto.subject()).isEqualTo(digitalInvoiceRequest.subject());
		assertThat(digitalInvoiceDto.reference()).isEqualTo(digitalInvoiceRequest.reference());
		assertThat(digitalInvoiceDto.payable()).isEqualTo(digitalInvoiceRequest.payable());

		assertThat(digitalInvoiceDto.details().amount()).isEqualTo(digitalInvoiceRequest.details().amount());
		assertThat(digitalInvoiceDto.details().dueDate()).isEqualTo(digitalInvoiceRequest.details().dueDate());
		assertThat(digitalInvoiceDto.details().paymentReferenceType()).isEqualTo(digitalInvoiceRequest.details().paymentReferenceType());
		assertThat(digitalInvoiceDto.details().paymentReference()).isEqualTo(digitalInvoiceRequest.details().paymentReference());
		assertThat(digitalInvoiceDto.details().accountType()).isEqualTo(digitalInvoiceRequest.details().accountType());
		assertThat(digitalInvoiceDto.details().accountNumber()).isEqualTo(digitalInvoiceRequest.details().accountNumber());

		// Verify that each attachment is there and mapped correctly
		assertThat(digitalInvoiceDto.files())
			.extracting(DigitalInvoiceDto.File::filename, DigitalInvoiceDto.File::contentType, DigitalInvoiceDto.File::content)
			.containsExactlyInAnyOrder(
				digitalInvoiceRequest.files().stream()
					.map(file -> tuple(file.filename(), file.contentType(), file.content()))
					.toArray(Tuple[]::new));
	}

	@Test
	void toWebMEssageDtoTest() {
		final var webMessageRequest = createValidWebMessageRequest().withOepInstance(OEP_INSTANCE_INTERNAL);
		final var webMessageDto = dtoMapper.toWebMessageDto(webMessageRequest);

		assertThat(webMessageDto.partyId()).isEqualTo(webMessageRequest.party().partyId());
		assertThat(webMessageDto.userId()).isEqualTo(webMessageRequest.sender().userId());
		assertThat(webMessageDto.externalReferences()).isEqualTo(webMessageRequest.party().externalReferences());
		assertThat(webMessageDto.message()).isEqualTo(webMessageRequest.message());
		assertThat(webMessageDto.oepInstance()).isEqualTo(webMessageRequest.oepInstance());

		// Verify that each attachment is there and mapped correctly
		assertThat(webMessageDto.attachments())
			.extracting(WebMessageDto.Attachment::fileName, WebMessageDto.Attachment::mimeType, WebMessageDto.Attachment::base64Data)
			.containsExactlyInAnyOrder(
				webMessageRequest.attachments().stream()
					.map(attachment -> tuple(attachment.fileName(), attachment.mimeType(), attachment.base64Data()))
					.toArray(Tuple[]::new));
	}

	@Test
	void toWebMEssageDtoWhenOepInstanceIsNullTest() {
		final var webMessageRequest = createValidWebMessageRequest();
		final var webMessageDto = dtoMapper.toWebMessageDto(webMessageRequest);

		assertThat(webMessageDto.partyId()).isEqualTo(webMessageRequest.party().partyId());
		assertThat(webMessageDto.userId()).isEqualTo(webMessageRequest.sender().userId());
		assertThat(webMessageDto.externalReferences()).isEqualTo(webMessageRequest.party().externalReferences());
		assertThat(webMessageDto.message()).isEqualTo(webMessageRequest.message());
		assertThat(webMessageDto.oepInstance()).isEqualTo(OEP_INSTANCE_EXTERNAL);

		// Verify that each attachment is there and mapped correctly
		assertThat(webMessageDto.attachments())
			.extracting(WebMessageDto.Attachment::fileName, WebMessageDto.Attachment::mimeType, WebMessageDto.Attachment::base64Data)
			.containsExactlyInAnyOrder(
				webMessageRequest.attachments().stream()
					.map(attachment -> tuple(attachment.fileName(), attachment.mimeType(), attachment.base64Data()))
					.toArray(Tuple[]::new));
	}

	@Test
	void toSnailMailDtoTest() {
		final var snailMailRequest = createValidSnailMailRequest();
		final var batchId = "batchId";

		// Not using the address from the request since it will be taken from the delivery.
		final var snailMailDto = dtoMapper.toSnailMailDto(snailMailRequest, batchId, createAddress());

		assertThat(snailMailDto.partyId()).isEqualTo(snailMailRequest.party().partyId());
		assertThat(snailMailDto.address()).isEqualTo(snailMailRequest.address());
		assertThat(snailMailDto.batchId()).isEqualTo(batchId);
		assertThat(snailMailDto.department()).isEqualTo(snailMailRequest.department());
		assertThat(snailMailDto.deviation()).isEqualTo(snailMailRequest.deviation());
		assertThat(snailMailDto.issuer()).isEqualTo(snailMailRequest.issuer());
		assertThat(snailMailDto.origin()).isEqualTo(snailMailRequest.origin());
		assertThat(snailMailDto.attachments()).hasSize(snailMailRequest.attachments().size());

		// Verify that each attachment is there and mapped correctly
		assertThat(snailMailDto.attachments())
			.extracting(SnailMailDto.Attachment::filename, SnailMailDto.Attachment::contentType, SnailMailDto.Attachment::content)
			.containsExactlyInAnyOrder(
				snailMailRequest.attachments().stream()
					.map(attachment -> tuple(attachment.filename(), attachment.contentType(), attachment.content()))
					.toArray(Tuple[]::new));
	}

	@Test
	void toSlackDtoTest() {
		final var slackRequest = createValidSlackRequest();

		final var slackDto = dtoMapper.toSlackDto(slackRequest);

		assertThat(slackDto.token()).isEqualTo(slackRequest.token());
		assertThat(slackDto.channel()).isEqualTo(slackRequest.channel());
		assertThat(slackDto.message()).isEqualTo(slackRequest.message());
	}
}

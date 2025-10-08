package se.sundsvall.messaging;

import static se.sundsvall.messaging.api.model.request.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.Header.REFERENCES;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.ANY;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.DIGITAL_MAIL;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageType.LETTER;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.Priority;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.Batch;
import se.sundsvall.messaging.api.model.response.UserBatches;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.StatisticEntity;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.ReferenceType;
import se.sundsvall.messaging.service.model.Attachment;

public final class TestDataFactory {

	public static final String DEFAULT_MOBILE_NUMBER = "+46701740605";
	public static final String DEFAULT_EMAIL_ADDRESS = "someone@somehost.com";
	public static final String DEFAULT_SENDER_NAME = "someSender";
	public static final String DEFAULT_SENDER_EMAIL_ADDRESS = "noreply@somehost.com";
	public static final String DEFAULT_SENDER_REPLY_TO = "replyTo@someone.com";
	public static final String HEADER_VALUE = "<test@test>";

	public static final String MUNICIPALITY_ID = "2281";
	public static final String ORGANIZATION_NUMBER = "2120002411";

	public static final String X_ORIGIN_HEADER = "x-origin";
	public static final String X_ORIGIN_HEADER_VALUE = "origin";

	public static final String X_ISSUER_HEADER = "x-issuer";
	public static final String X_ISSUER_HEADER_VALUE = "issuer";

	public static final String X_SENT_BY_HEADER = "X-Sent-By";
	public static final String X_SENT_BY_HEADER_VALUE = "type=adAccount; joe01doe";
	public static final String X_SENT_BY_HEADER_USER_NAME = "joe01doe";

	private TestDataFactory() {}

	public static Message createMessage(final MessageType type, final String content) {
		return createMessage(UUID.randomUUID().toString(), type, content);
	}

	public static Message createMessage(final String partyId, final MessageType type, final String content) {
		return Message.builder()
			.withBatchId(UUID.randomUUID().toString())
			.withMessageId(UUID.randomUUID().toString())
			.withDeliveryId(UUID.randomUUID().toString())
			.withPartyId(partyId)
			.withType(type)
			.withOriginalType(type)
			.withContent(content)
			.withStatus(PENDING)
			.build();
	}

	public static Address createAddress() {
		return Address.builder()
			.withFirstName("someFirstName")
			.withLastName("someLastName")
			.withAddress("someAddress")
			.withApartmentNumber("someApartmentNumber")
			.withCareOf("someCareOf")
			.withZipCode("12345")
			.withCity("someCity")
			.withCountry("someCountry")
			.build();
	}

	public static EmailRequest createValidEmailRequest() {
		return EmailRequest.builder()
			.withParty(EmailRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withSender(EmailRequest.Sender.builder()
				.withName(DEFAULT_SENDER_NAME)
				.withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
				.build())
			.withEmailAddress(DEFAULT_EMAIL_ADDRESS)
			.withSubject("someSubject")
			.withMessage("someMessage")
			.withHtmlMessage("someHtmlMessage")
			.withHeaders(Map.of(
				MESSAGE_ID.name(), List.of("<someMessageId@test.com>"),
				REFERENCES.name(), List.of("<someReferences@test.com>", "<someMoreReferences@test.com>"),
				IN_REPLY_TO.name(), List.of("<someInReplyTo@test.com>")))
			.withAttachments(List.of(
				EmailRequest.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("aGVsbG8gd29ybGQK")
					.build()))
			.build();
	}

	public static EmailBatchRequest.Party createValidEmailBatchRequestParty() {
		return EmailBatchRequest.Party.builder()
			.withEmailAddress(DEFAULT_EMAIL_ADDRESS)
			.withPartyId(UUID.randomUUID().toString())
			.build();
	}

	public static EmailBatchRequest.Sender createValidEmailBatchRequestSender() {
		return EmailBatchRequest.Sender.builder()
			.withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
			.withName(DEFAULT_SENDER_NAME)
			.withReplyTo(DEFAULT_SENDER_REPLY_TO)
			.build();
	}

	public static EmailBatchRequest.Attachment createValidEmailBatchRequestAttachment() {
		return EmailBatchRequest.Attachment.builder()
			.withContentType("text/plain")
			.withContent("c29tZUJhc2U2NENvbnRlbnQ=")
			.withName("someName")
			.build();
	}

	public static Map<String, List<String>> createValidHeaders() {
		return Map.of(MESSAGE_ID.name(), List.of(HEADER_VALUE),
			REFERENCES.name(), List.of(HEADER_VALUE, HEADER_VALUE),
			IN_REPLY_TO.name(), List.of(HEADER_VALUE));

	}

	public static EmailBatchRequest createValidEmailBatchRequest() {
		return EmailBatchRequest.builder()
			.withParties(List.of(createValidEmailBatchRequestParty(), createValidEmailBatchRequestParty()))
			.withHeaders(createValidHeaders())
			.withHtmlMessage("someHtmlMessage")
			.withMessage("someMessage")
			.withSubject("someSubject")
			.withSender(createValidEmailBatchRequestSender())
			.withAttachments(List.of(createValidEmailBatchRequestAttachment()))
			.build();
	}

	public static SnailMailRequest createValidSnailMailRequest() {
		return SnailMailRequest.builder()
			.withParty(SnailMailRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withDepartment("someDepartment")
			.withDeviation("someDeviation")
			.withAttachments(List.of(
				SnailMailRequest.Attachment.builder()
					.withFilename("someName")
					.withContentType("someContentType")
					.withContent("someContent")
					.build()))
			.withAddress(createAddress())
			.withIssuer("someIssuer")
			.withOrigin("someOrigin")
			.withFolderName("someFolderName")
			.build();
	}

	public static SmsRequest createValidSmsRequest() {
		return SmsRequest.builder()
			.withParty(SmsRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withSender(DEFAULT_SENDER_NAME)
			.withMobileNumber(DEFAULT_MOBILE_NUMBER)
			.withMessage("someMessage")
			.withPriority(Priority.HIGH)
			.build();
	}

	public static SmsBatchRequest createValidSmsBatchRequest() {
		return SmsBatchRequest.builder()
			.withParties(List.of(SmsBatchRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withMobileNumber(DEFAULT_MOBILE_NUMBER)
				.build()))
			.withSender(DEFAULT_SENDER_NAME)
			.withMessage("someMessage")
			.build();
	}

	public static WebMessageRequest createValidWebMessageRequest() {
		return WebMessageRequest.builder()
			.withParty(WebMessageRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withMessage("someMessage")
			.withAttachments(List.of(
				WebMessageRequest.Attachment.builder()
					.withFileName("someFileName")
					.withMimeType("text/plain")
					.withBase64Data("bG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQK")
					.build()))
			.withSender(WebMessageRequest.Sender.builder()
				.withUserId(UUID.randomUUID().toString())
				.build())
			.build();
	}

	public static DigitalMailRequest createValidDigitalMailRequest() {
		return DigitalMailRequest.builder()
			.withParty(DigitalMailRequest.Party.builder()
				.withPartyIds(List.of(UUID.randomUUID().toString()))
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withSender(DigitalMailRequest.Sender.builder()
				.withSupportInfo(DigitalMailRequest.Sender.SupportInfo.builder()
					.withText("someText")
					.withUrl("someUrl")
					.withPhoneNumber("somePhoneNumber")
					.withEmailAddress("someone@somehost.com")
					.build())
				.build())
			.withSubject("someSubject")
			.withContentType(ContentType.TEXT_PLAIN.getValue())
			.withBody("someBody")
			.withDepartment("someDepartment")
			.withAttachments(List.of(
				DigitalMailRequest.Attachment.builder()
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withContent("someContent")
					.withFilename("someFilename")
					.build()))
			.build();
	}

	public static DigitalInvoiceRequest createValidDigitalInvoiceRequest() {
		return DigitalInvoiceRequest.builder()
			.withParty(DigitalInvoiceRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withType(InvoiceType.INVOICE)
			.withSubject("someSubject")
			.withReference("someReference")
			.withDetails(DigitalInvoiceRequest.Details.builder()
				.withAmount(12.34f)
				.withDueDate(LocalDate.now().plusDays(30))
				.withPaymentReferenceType(ReferenceType.SE_OCR)
				.withPaymentReference("somePaymentReference")
				.withAccountType(AccountType.BANKGIRO)
				.withAccountNumber("1234567")
				.build())
			.withFiles(List.of(
				DigitalInvoiceRequest.File.builder()
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withContent("c29tZUNvbnRlbnQK")
					.withFilename("someFilename")
					.build()))
			.build();
	}

	public static LetterRequest createValidLetterRequest() {
		return LetterRequest.builder()
			.withParty(LetterRequest.Party.builder()
				.withPartyIds(List.of(UUID.randomUUID().toString()))
				.withExternalReferences(List.of(createExternalReference()))
				.withAddresses(List.of(createAddress()))
				.build())
			.withSender(LetterRequest.Sender.builder()
				.withSupportInfo(LetterRequest.Sender.SupportInfo.builder()
					.withText("someText")
					.withUrl("someUrl")
					.withPhoneNumber("somePhoneNumber")
					.withEmailAddress("someone@somehost.com")
					.build())
				.build())
			.withSubject("someSubject")
			.withContentType(ContentType.TEXT_PLAIN.getValue())
			.withBody("someBody")
			.withDepartment("someDepartment")
			.withAttachments(List.of(
				LetterRequest.Attachment.builder()
					.withDeliveryMode(DIGITAL_MAIL)
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withFilename("someFilename")
					.withContent("someContent")
					.build(),
				LetterRequest.Attachment.builder()
					.withDeliveryMode(SNAIL_MAIL)
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withFilename("someFilename")
					.withContent("someContent")
					.build(),
				LetterRequest.Attachment.builder()
					.withDeliveryMode(ANY)
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withFilename("someFilename")
					.withContent("someContent")
					.build()))
			.build();
	}

	public static MessageRequest createValidMessageRequest(final List<String> partyIds) {
		return MessageRequest.builder()
			.withMessages(partyIds.stream()
				.map(TestDataFactory::createValidMessageRequestMessage)
				.toList())
			.build();
	}

	public static MessageRequest.Message createValidMessageRequestMessage() {
		return createValidMessageRequestMessage(UUID.randomUUID().toString());
	}

	public static MessageRequest.Message createValidMessageRequestMessage(final String partyId) {
		return MessageRequest.Message.builder()
			.withParty(MessageRequest.Message.Party.builder()
				.withPartyId(partyId)
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withSender(MessageRequest.Message.Sender.builder()
				.withEmail(MessageRequest.Message.Sender.Email.builder()
					.withName(DEFAULT_SENDER_NAME)
					.withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
					.build())
				.withSms(MessageRequest.Message.Sender.Sms.builder()
					.withName(DEFAULT_SENDER_NAME)
					.build())
				.build())
			.withSubject("someSubject")
			.withMessage("someMessage")
			.build();
	}

	public static SlackRequest createValidSlackRequest() {
		return SlackRequest.builder()
			.withToken("someToken")
			.withChannel("someChannel")
			.withMessage("someMessage")
			.withMunicipalityId(MUNICIPALITY_ID)
			.build();
	}

	public static ExternalReference createExternalReference() {
		return ExternalReference.builder()
			.withKey("someKey")
			.withValue("someValue")
			.build();
	}

	public static EmailDto createEmailDto() {
		return EmailDto.builder()
			.withSender(EmailDto.Sender.builder()
				.withName(DEFAULT_SENDER_NAME)
				.withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
				.withReplyTo("someReplyTo")
				.build())
			.withEmailAddress(DEFAULT_EMAIL_ADDRESS)
			.withSubject("someSubject")
			.withMessage("someMessage")
			.withHtmlMessage("someHtmlMessage")
			.withHeaders(Map.of("MESSAGE_ID", List.of("someMessageId"),
				"REFERENCES", List.of("someReferences", "someMoreReferences"),
				"IN_REPLY_TO", List.of("someInReplyTo")))
			.withAttachments(List.of(
				EmailDto.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("aGVsbG8gd29ybGQK")
					.build()))
			.build();
	}

	public static EmailBatchRequest createEmailBatchRequest() {
		return EmailBatchRequest.builder()
			.withAttachments(List.of(
				EmailBatchRequest.Attachment.builder()
					.withContentType("text/plain")
					.withContent("c29tZUJhc2U2NENvbnRlbnQ=")
					.withName("someName")
					.build()))
			.withSender(EmailBatchRequest.Sender.builder()
				.withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
				.withName(DEFAULT_SENDER_NAME)
				.withReplyTo("test@testorsson.com")
				.build())
			.withHeaders(Map.of(MESSAGE_ID.name(), List.of(HEADER_VALUE),
				REFERENCES.name(), List.of(HEADER_VALUE, HEADER_VALUE),
				IN_REPLY_TO.name(), List.of(HEADER_VALUE)))
			.withParties(List.of(createValidEmailBatchRequestParty(), createValidEmailBatchRequestParty()))
			.withHtmlMessage("someHtmlMessage")
			.withSubject("someSubject")
			.build();
	}

	public static Batch createBatch() {
		return Batch.builder()
			.withAttachmentCount(5)
			.withBatchId("someBatchId")
			.withMessageType(LETTER.toString())
			.withRecipientCount(11)
			.withSent(LocalDateTime.now())
			.withStatus(Batch.Status.builder()
				.withSuccessful(8)
				.withUnsuccessful(3)
				.build())
			.withSubject(DEFAULT_EMAIL_ADDRESS)
			.build();
	}

	public static UserBatches createUserBatches() {
		return UserBatches.builder()
			.withMetaData(createPagingMetaData())
			.withBatches(List.of(createBatch(), createBatch()))
			.build();
	}

	public static UserMessage createUserMessage() {
		return UserMessage.builder()
			.withSent(LocalDateTime.now())
			.withRecipients(List.of())
			.withAttachments(List.of())
			.withSubject("someSubject")
			.withOrigin("someOrigin")
			.withIssuer("someIssuer")
			.withMessageId("someMessageId")
			.build();
	}

	public static PagingMetaData createPagingMetaData() {
		return new PagingMetaData()
			.withPage(1)
			.withLimit(10)
			.withCount(10)
			.withTotalRecords(100)
			.withTotalPages(10);
	}

	public static UserMessages createUserMessages() {
		return UserMessages.builder()
			.withMetaData(createPagingMetaData())
			.withMessages(List.of(createUserMessage(), createUserMessage()))
			.build();
	}

	public static HistoryEntity createHistoryEntity() {
		return HistoryEntity.builder()
			.withId(123L)
			.withMunicipalityId("2281")
			.withMessageId(UUID.randomUUID().toString())
			.withStatus(MessageStatus.SENT)
			.withIssuer("someIssuer")
			.withContent("""
				    {
				    "attachments": [
				        {
				            "fileName": "someFileName",
				            "content": "someContent",
				            "contentType": "application/pdf"
				        }
				    ],
				    "subject": "someSubject"
				}
				""")
			.withBatchId("someBatchId")
			.withDeliveryId("someDeliveryId")
			.withCreatedAt(LocalDateTime.now())
			.withMessageType(MessageType.SNAIL_MAIL)
			.withDestinationAddress(createAddress())
			.withPartyId("somePartyId")
			.build();
	}

	public static Attachment createAttachment() {
		return Attachment.builder()
			.withName("someName")
			.withContent("someContent")
			.withContentType("someContentType")
			.build();
	}

	public static StatisticEntity createStatisticsEntity(MessageType messageType, MessageType originalMessageType, MessageStatus messageStatus, String origin, String department, String municipalityId) {
		return StatisticEntity.builder()
			.withMessageType(messageType)
			.withOriginalMessageType(originalMessageType)
			.withStatus(messageStatus)
			.withOrigin(origin)
			.withDepartment(department)
			.withMunicipalityId(municipalityId)
			.build();
	}
}

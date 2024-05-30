package se.sundsvall.messaging;

import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.IN_REPLY_TO;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.MESSAGE_ID;
import static se.sundsvall.messaging.api.model.request.EmailRequest.Header.REFERENCES;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.ANY;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.DIGITAL_MAIL;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.model.AccountType;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.InvoiceType;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.ReferenceType;

public final class TestDataFactory {

	public static final String DEFAULT_MOBILE_NUMBER = "+46701234567";

	public static final String DEFAULT_EMAIL_ADDRESS = "someone@somehost.com";

	public static final String DEFAULT_SENDER_NAME = "someSender";
	public static final String DEFAULT_SENDER_EMAIL_ADDRESS = "noreply@somehost.com";

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
			.withOrigin("someOrigin")
			.withHeaders(Map.of(MESSAGE_ID, List.of("<someMessageId@test.com>"),
				REFERENCES, List.of("<someReferences@test.com>", "<someMoreReferences@test.com>"),
				IN_REPLY_TO, List.of("<someInReplyTo@test.com>")))
			.withAttachments(List.of(
				EmailRequest.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("aGVsbG8gd29ybGQK")
					.build()))
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
			.withOrigin("someOrigin")
			.withAttachments(List.of(
				SnailMailRequest.Attachment.builder()
					.withName("someName")
					.withContentType("someContentType")
					.withContent("someContent")
					.build()))
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
			.withOrigin("someOrigin")
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
			.withOrigin("someOrigin")
			.build();
	}

	public static WebMessageRequest createValidWebMessageRequest() {
		return WebMessageRequest.builder()
			.withParty(WebMessageRequest.Party.builder()
				.withPartyId(UUID.randomUUID().toString())
				.withExternalReferences(List.of(createExternalReference()))
				.build())
			.withMessage("someMessage")
			.withOrigin("someOrigin")
			.withAttachments(List.of(
				WebMessageRequest.Attachment.builder()
					.withFileName("someFileName")
					.withMimeType("text/plain")
					.withBase64Data("bG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQK")
					.build()))
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
			.withOrigin("someOrigin")
			.withAttachments(List.of(
				DigitalMailRequest.Attachment.builder()
					.withContentType(ContentType.APPLICATION_PDF.getValue())
					.withContent("someContent")
					.withFilename("someFilename")
					.build()
			))
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
					.build()
			))
			.build();
	}

	public static LetterRequest createValidLetterRequest() {
		return LetterRequest.builder()
			.withParty(LetterRequest.Party.builder()
				.withPartyIds(List.of(UUID.randomUUID().toString()))
				.withExternalReferences(List.of(createExternalReference()))
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
					.build()
			))
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
			.withOrigin("someOrigin")
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
}

package se.sundsvall.messaging;

import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.DIGITAL_MAIL;
import static se.sundsvall.messaging.api.model.request.LetterRequest.Attachment.DeliveryMode.SNAIL_MAIL;

import java.util.List;
import java.util.UUID;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;

import generated.se.sundsvall.messagingrules.HeaderName;

public final class TestDataFactory {

    public static final String DEFAULT_PARTY_ID = UUID.randomUUID().toString();

    public static final String DEFAULT_MOBILE_NUMBER = "0701234567";

    public static final String DEFAULT_EMAIL_ADDRESS = "someone@somehost.com";

    public static final String DEFAULT_SENDER_NAME = "someSender";
    public static final String DEFAULT_SENDER_EMAIL_ADDRESS = "noreply@somehost.com";
    
    private TestDataFactory() { }

    public static EmailRequest createValidEmailRequest() {
        return EmailRequest.builder()
            .withParty(EmailRequest.Party.builder()
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
            .withSender(EmailRequest.Sender.builder()
                .withName(DEFAULT_SENDER_NAME)
                .withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
                .build())
            .withEmailAddress(DEFAULT_EMAIL_ADDRESS)
            .withSubject("someSubject")
            .withMessage("someMessage")
            .withHtmlMessage("someHtmlMessage")
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
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
            .withDepartment("someDepartment")
            .withDeviation("someDeviation")
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
            .withHeaders(List.of(createHeader()))
            .withSender(DEFAULT_SENDER_NAME)
            .withMobileNumber(DEFAULT_MOBILE_NUMBER)
            .withMessage("someMessage")
            .build();
    }

    public static WebMessageRequest createValidWebMessageRequest() {
        return WebMessageRequest.builder()
            .withParty(WebMessageRequest.Party.builder()
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
            .withMessage("someMessage")
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
                .withPartyIds(List.of(DEFAULT_PARTY_ID))
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
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
            .withAttachments(List.of(
                DigitalMailRequest.Attachment.builder()
                    .withContentType(ContentType.APPLICATION_PDF.getValue())
                    .withContent("someContent")
                    .withFilename("someFilename")
                    .build()
                ))
            .build();
    }

    public static LetterRequest createValidLetterRequest() {
        return LetterRequest.builder()
            .withParty(LetterRequest.Party.builder()
                .withPartyIds(List.of(DEFAULT_PARTY_ID))
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
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
                    .withContent("someContent")
                    .withFilename("someFilename")
                    .build(),
                LetterRequest.Attachment.builder()
                    .withDeliveryMode(SNAIL_MAIL)
                    .withContentType(ContentType.APPLICATION_PDF.getValue())
                    .withContent("someContent")
                    .withFilename("someFilename")
                    .build()
                ))
            .build();
    }

    public static MessageRequest.Message createValidMessageRequest() {
        return MessageRequest.Message.builder()
            .withParty(MessageRequest.Message.Party.builder()
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withHeaders(List.of(createHeader()))
            .withSubject("someSubject")
            .withMessage("someMessage")
            .build();
    }

    public static ExternalReference createExternalReference() {
        return ExternalReference.builder()
            .withKey("someKey")
            .withValue("someValue")
            .build();
    }

    public static Header createHeader() {
        return Header.builder()
            .withName(HeaderName.CATEGORY)
            .withValues(List.of("someValue1", "someValue2"))
            .build();
    }
}

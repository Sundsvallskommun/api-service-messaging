package se.sundsvall.messaging;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.model.Parties;
import se.sundsvall.messaging.model.Party;
import se.sundsvall.messaging.model.Sender;

import generated.se.sundsvall.messagingrules.HeaderName;

public final class TestDataFactory {

    public static final String DEFAULT_PARTY_ID = UUID.randomUUID().toString();

    public static final String DEFAULT_MOBILE_NUMBER = "0701234567";

    public static final String DEFAULT_EMAIL_ADDRESS = "someone@somehost.com";

    public static final String DEFAULT_SENDER_NAME = "someSender";
    public static final String DEFAULT_SENDER_EMAIL_ADDRESS = "noreply@somehost.com";
    
    private TestDataFactory() { }

    public static EmailRequest createEmailRequest() {
        return createEmailRequest(null);
    }

    public static EmailRequest createEmailRequest(final Consumer<EmailRequest> modifier) {
        var request = EmailRequest.builder()
                .withParty(Party.builder()
                        .withPartyId(DEFAULT_PARTY_ID)
                        .withExternalReferences(List.of(ExternalReference.builder()
                                .withKey("someKey")
                                .withValue("someValue")
                                .build()))
                        .build())
                .withHeaders(List.of(Header.builder()
                        .withName(HeaderName.CATEGORY)
                        .withValues(List.of("someValue1", "someValue2"))
                        .build()))
                .withSender(Sender.Email.builder()
                        .withName(DEFAULT_SENDER_NAME)
                        .withAddress(DEFAULT_SENDER_EMAIL_ADDRESS)
                        .build())
                .withEmailAddress(DEFAULT_EMAIL_ADDRESS)
                .withSubject("someSubject")
                .withMessage("someMessage")
                .withHtmlMessage("someHtmlMessage")
                .withAttachments(List.of(EmailRequest.Attachment.builder()
                        .withName("someName")
                        .withContentType("someContentType")
                        .withContent("someContent")
                        .build()))
                .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }


    public static SnailmailRequest createSnailmailRequest() {
        return createSnailmailRequest(null);
    }

    public static SnailmailRequest createSnailmailRequest(final Consumer<SnailmailRequest> modifier) {
        var request = SnailmailRequest.builder()
                .withParty(Party.builder()
                        .withPartyId(DEFAULT_PARTY_ID)
                        .withExternalReferences(List.of(ExternalReference.builder()
                                .withKey("someKey")
                                .withValue("someValue")
                                .build()))
                        .build())
                .withHeaders(List.of(Header.builder()
                        .withName(HeaderName.CATEGORY)
                        .withValues(List.of("someValue1", "someValue2"))
                        .build()))
                .withPersonId("58f96da8-6d76-4fa6-bb92-64f71fdc6aa5")
                .withDepartment("someDepartment")
                .withDeviation("someDeviation")
                .withAttachments(List.of(SnailmailRequest.Attachment.builder()
                        .withName("someName")
                        .withContentType("someContentType")
                        .withContent("someContent")
                        .build()))
                .build();
        if (modifier != null) {
            modifier.accept(request);
        }
        return request;
    }

    public static SmsRequest createSmsRequest() {
        return createSmsRequest(null);
    }

    public static SmsRequest createSmsRequest(final Consumer<SmsRequest> modifier) {
        var request = SmsRequest.builder()
                .withParty(Party.builder()
                        .withPartyId(DEFAULT_PARTY_ID)
                        .withExternalReferences(List.of(ExternalReference.builder()
                                .withKey("someKey")
                                .withValue("someValue")
                                .build()))
                        .build())
            .withHeaders(List.of(Header.builder()
                .withName(HeaderName.CATEGORY)
                .withValues(List.of("someValue1", "someValue2"))
                .build()))
            .withSender(Sender.Sms.builder()
                .withName(DEFAULT_SENDER_NAME)
                .build())
            .withMobileNumber(DEFAULT_MOBILE_NUMBER)
            .withMessage("someMessage")
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }

    public static WebMessageRequest createWebMessageRequest() {
        return createWebMessageRequest(null);
    }

    public static WebMessageRequest createWebMessageRequest(final Consumer<WebMessageRequest> modifier) {
        var request = WebMessageRequest.builder()
            .withParty(Party.builder()
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(ExternalReference.builder()
                    .withKey("someKey")
                    .withValue("someValue")
                    .build()))
                .build())
            .withHeaders(List.of(Header.builder()
                .withName(HeaderName.CATEGORY)
                .withValues(List.of("someValue1", "someValue2"))
                .build()))
            .withMessage("someMessage")
            .withAttachments(List.of(WebMessageRequest.Attachment.builder()
                .withFileName("someFileName")
                .withMimeType("text/plain")
                .withBase64Data("bG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQK")
                .build()))
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }

    public static DigitalMailRequest createDigitalMailRequest() {
        return createDigitalMailRequest(null);
    }

    public static DigitalMailRequest createDigitalMailRequest(final Consumer<DigitalMailRequest> modifier) {
        var request = DigitalMailRequest.builder()
            .withParty(Parties.builder()
                .withPartyIds(List.of(DEFAULT_PARTY_ID))
                .withExternalReferences(List.of(ExternalReference.builder()
                    .withKey("someKey")
                    .withValue("someValue")
                    .build()))
                .build())
            .withHeaders(List.of(Header.builder()
                .withName(HeaderName.TYPE)
                .withValues(List.of("someValue1", "someValue2"))
                .build()))
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

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }

    public static MessageRequest.Message createMessageRequest() {
        return createMessageRequest(null);
    }

    public static MessageRequest.Message createMessageRequest(final Consumer<MessageRequest.Message> modifier) {
        var request = MessageRequest.Message.builder()
            .withParty(Party.builder()
                .withPartyId(DEFAULT_PARTY_ID)
                .withExternalReferences(List.of(ExternalReference.builder()
                    .withKey("someKey")
                    .withValue("someValue")
                    .build()))
                .build())
            .withHeaders(List.of(Header.builder()
                .withName(HeaderName.DISTRIBUTION_RULE)
                .withValues(List.of("someValue1", "someValue2"))
                .build()))
            .withSubject("someSubject")
            .withMessage("someMessage")
            .build();

        if (modifier != null) {
            modifier.accept(request);
        }

        return request;
    }

    public static ExternalReference createExternalReference() {
        return createExternalReference(null);
    }

    public static ExternalReference createExternalReference(final Consumer<ExternalReference> modifier) {
        var externalReference = ExternalReference.builder()
            .withKey("someKey")
            .withValue("someValue")
            .build();

        if (modifier != null) {
            modifier.accept(externalReference);
        }

        return externalReference;
    }

    public static Header createHeader() {
        return createHeader(null);
    }

    public static Header createHeader(final Consumer<Header> modifier) {
        var header = Header.builder()
            .withName(HeaderName.TYPE)
            .withValues(List.of("someValue1", "someValue2"))
            .build();

        if (modifier != null) {
            modifier.accept(header);
        }

        return header;
    }

    public static Party createParty() {
        return createParty(null);
    }

    public static Party createParty(final Consumer<Party> modifier) {
        var party = Party.builder()
            .withPartyId(UUID.randomUUID().toString())
            .withExternalReferences(List.of(ExternalReference.builder()
                .withKey("someKey")
                .withValue("someValue")
                .build()))
            .build();

        if (modifier != null) {
            modifier.accept(party);
        }

        return party;
    }

    public static Sender createSender() {
        return createSender(null);
    }

    public static Sender createSender(final Consumer<Sender> modifier) {
        var sender = Sender.builder()
            .withSms(Sender.Sms.builder()
                .withName("someName")
                .build())
            .withEmail(Sender.Email.builder()
                .withName("someName")
                .withAddress("someone@somehost.com")
                .withReplyTo("replyto@somehost.com")
                .build())
            .build();

        if (modifier != null) {
            modifier.accept(sender);
        }

        return sender;
    }
}

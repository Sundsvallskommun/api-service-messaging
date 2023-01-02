package se.sundsvall.messaging.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailDto;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.integration.smssender.SmsDto;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailDto;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageDto;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@Component
class MessageMapper {

    static final Gson GSON = new GsonBuilder().create();

    private final String defaultSmsDtoSender;
    private final String defaultSmsRequestSender;
    private final EmailDto.Sender defaultEmailDtoSender;
    private final EmailRequest.Sender defaultEmailRequestSender;
    private final String defaultDigitalMailDtoMunicipalityId;
    private final DigitalMailDto.Sender.SupportInfo defaultDigitalMailDtoSenderSupportInfo;

    MessageMapper(final Defaults defaults) {
        defaultSmsDtoSender = defaults.sms().name();
        defaultSmsRequestSender = defaults.sms().name();

        defaultEmailDtoSender = EmailDto.Sender.builder()
            .withName(defaults.email().name())
            .withAddress(defaults.email().address())
            .withReplyTo(defaults.email().replyTo())
            .build();

        defaultEmailRequestSender = EmailRequest.Sender.builder()
            .withName(defaults.email().name())
            .withAddress(defaults.email().address())
            .withReplyTo(defaults.email().replyTo())
            .build();

        defaultDigitalMailDtoMunicipalityId = defaults.digitalMail().municipalityId();
        defaultDigitalMailDtoSenderSupportInfo = DigitalMailDto.Sender.SupportInfo.builder()
            .withText(defaults.digitalMail().supportInfo().text())
            .withEmailAddress(defaults.digitalMail().supportInfo().emailAddress())
            .withPhoneNumber(defaults.digitalMail().supportInfo().phoneNumber())
            .withUrl(defaults.digitalMail().supportInfo().url())
            .build();
    }

    String toSmsRequest(final Message message, final String mobileNumber) {
        var originalMessage = GSON.fromJson(message.content(), MessageRequest.Message.class);

        var sender = Optional.ofNullable(originalMessage.sender())
            .map(MessageRequest.Message.Sender::sms)
            .map(MessageRequest.Message.Sender.Sms::name)
            .orElse(defaultSmsRequestSender);

        var smsRequest = SmsRequest.builder()
            .withParty(SmsRequest.Party.builder()
                .withPartyId(Optional.ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::partyId)
                    .orElse(null))
                .withExternalReferences(Optional.ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::externalReferences)
                    .orElse(null))
                .build())
            .withHeaders(originalMessage.headers())
            .withSender(sender)
            .withMobileNumber(mobileNumber)
            .withMessage(originalMessage.message())
            .build();

        return GSON.toJson(smsRequest);
    }

    String toEmailRequest(final Message message, final String emailAddress) {
        var originalMessage = GSON.fromJson(message.content(), MessageRequest.Message.class);

        var sender = Optional.ofNullable(originalMessage.sender())
            .map(MessageRequest.Message.Sender::email)
            .map(emailSender -> EmailRequest.Sender.builder()
                .withName(emailSender.name())
                .withAddress(emailSender.address())
                .withReplyTo(emailSender.replyTo())
                .build())
            .orElse(defaultEmailRequestSender);

        var emailRequest = EmailRequest.builder()
            .withParty(EmailRequest.Party.builder()
                .withPartyId(Optional.ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::partyId)
                    .orElse(null))
                .withExternalReferences(Optional.ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::externalReferences)
                    .orElse(null))
                .build())
            .withHeaders(originalMessage.headers())
            .withSender(sender)
            .withEmailAddress(emailAddress)
            .withSubject(originalMessage.subject())
            .withMessage(originalMessage.message())
            .withHtmlMessage(originalMessage.htmlMessage())
            .build();

        return GSON.toJson(emailRequest);
    }

    DigitalMailRequest toDigitalMailRequest(final LetterRequest request) {
        return DigitalMailRequest.builder()
            .withSender(DigitalMailRequest.Sender.builder()
                .withSupportInfo(Optional.ofNullable(request.sender()).map(LetterRequest.Sender::supportInfo)
                    .map(supportInfo -> DigitalMailRequest.Sender.SupportInfo.builder()
                        .withText(supportInfo.text())
                        .withUrl(supportInfo.url())
                        .withEmailAddress(supportInfo.emailAddress())
                        .withPhoneNumber(supportInfo.phoneNumber())
                        .build())
                    .orElse(null))
                .build())
            .withParty(DigitalMailRequest.Party.builder()
                // TODO: nullcheck x 2
                .withPartyIds(request.party().partyIds())
                .withExternalReferences(request.party().externalReferences())
                .build())
            .withHeaders(request.headers())
            .withContentType(request.contentType())
            .withSubject(request.subject())
            .withBody(request.body())
            // TODO: nullcheck
            .withAttachments(request.attachments().stream()
                .filter(LetterRequest.Attachment::isIntendedForDigitalMail)
                .map(attachment -> DigitalMailRequest.Attachment.builder()
                    .withFilename(attachment.filename())
                    .withContent(attachment.content())
                    .withContentType(attachment.contentType())
                    .build())
                .toList())
            .build();
    }

    SnailMailRequest toSnailMailRequest(final LetterRequest request) {
        return SnailMailRequest.builder()
            .withHeaders(request.headers())
            .withDepartment(request.department())
            .withDeviation(request.deviation())
            .withAttachments(request.attachments().stream()
                .filter(LetterRequest.Attachment::isIntendedForSnailMail)
                .map(attachment -> SnailMailRequest.Attachment.builder()
                    .withName(attachment.filename())
                    .withContent(attachment.content())
                    .withContentType(attachment.contentType())
                    .build())
                .toList())
            .build();
    }

    SmsDto toSmsDto(final SmsRequest request) {
        return SmsDto.builder()
            .withSender(Optional.ofNullable(request.sender())
                .orElse(defaultSmsDtoSender))
            .withMobileNumber(request.mobileNumber())
            .withMessage(request.message())
            .build();
    }

    EmailDto toEmailDto(final EmailRequest request) {
        return EmailDto.builder()
            .withSender(Optional.ofNullable(request.sender())
                .map(requestSender -> EmailDto.Sender.builder()
                    .withName(requestSender.name())
                    .withAddress(requestSender.address())
                    .withReplyTo(requestSender.replyTo())
                    .build())
                .orElse(defaultEmailDtoSender))
            .withEmailAddress(request.emailAddress())
            .withSubject(request.subject())
            .withMessage(request.message())
            .withHtmlMessage(request.htmlMessage())
            .withAttachments(Optional.ofNullable(request.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> EmailDto.Attachment.builder()
                        .withName(attachment.name())
                        .withContentType(attachment.contentType())
                        .withContent(attachment.content())
                        .build())
                    .toList())
                .orElse(null))
            .build();
    }

    DigitalMailDto toDigitalMailDto(final DigitalMailRequest request, final String partyId) {
        return DigitalMailDto.builder()
            .withSender(DigitalMailDto.Sender.builder()
                .withMunicipalityId(defaultDigitalMailDtoMunicipalityId)
                .withSupportInfo(Optional.ofNullable(request.sender()).map(DigitalMailRequest.Sender::supportInfo)
                    .map(supportInfo -> DigitalMailDto.Sender.SupportInfo.builder()
                        .withText(supportInfo.text())
                        .withEmailAddress(supportInfo.emailAddress())
                        .withPhoneNumber(supportInfo.phoneNumber())
                        .withUrl(supportInfo.url())
                        .build())
                    .orElse(defaultDigitalMailDtoSenderSupportInfo))
                .build())
            .withPartyId(partyId)
            .withContentType(ContentType.fromString(request.contentType()))
            .withSubject(request.subject())
            .withBody(request.body())
            .withAttachments(Optional.ofNullable(request.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> DigitalMailDto.Attachment.builder()
                        .withFilename(attachment.filename())
                        .withContentType(ContentType.fromString(attachment.contentType()))
                        .withContent(attachment.content())
                        .build())
                    .toList())
                    .orElse(null))
            .build();
    }

    WebMessageDto toWebMessageDto(final WebMessageRequest request) {
        return WebMessageDto.builder()
            .withPartyId(Optional.ofNullable(request.party())
                .map(WebMessageRequest.Party::partyId)
                .orElse(null))
            .withExternalReferences(Optional.ofNullable(request.party())
                .map(WebMessageRequest.Party::externalReferences)
                .orElse(null))
            .withMessage(request.message())
            .withAttachments(Optional.ofNullable(request.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> WebMessageDto.Attachment.builder()
                        .withFileName(attachment.fileName())
                        .withMimeType(attachment.mimeType())
                        .withBase64Data(attachment.base64Data())
                        .build())
                    .toList())
                .orElse(null))
            .build();
    }

    SnailMailDto toSnailmailDto(final SnailMailRequest request) {
        return SnailMailDto.builder()
            .withDepartment(request.department())
            .withDeviation(request.deviation())
            .withAttachments(Optional.ofNullable(request.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> SnailMailDto.Attachment.builder()
                        .withName(attachment.name())
                        .withContentType(attachment.contentType())
                        .withContent(attachment.content())
                        .build())
                    .toList())
                .orElse(null))
            .build();
    }

    Message toMessage(final EmailRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(EmailRequest.Party::partyId)
                .orElse(null))
            .withType(MessageType.EMAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    Message toMessage(final SmsRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(SmsRequest.Party::partyId)
                .orElse(null))
            .withType(MessageType.SMS)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    Message toMessage(final SnailMailRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(SnailMailRequest.Party::partyId)
                .orElse(null))
            .withType(MessageType.SNAIL_MAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }


    Message toMessage(final WebMessageRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(WebMessageRequest.Party::partyId)
                .orElse(null))
            .withType(MessageType.WEB_MESSAGE)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    List<Message> toMessages(final DigitalMailRequest request, final String batchId) {
        var messageId = UUID.randomUUID().toString();

        return request.party().partyIds().stream()
            .map(partyId -> Message.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withDeliveryId(UUID.randomUUID().toString())
                .withPartyId(partyId)
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build())
            .toList();
    }

    List<Message> toMessages(final LetterRequest request, final String batchId) {
        var messageId = UUID.randomUUID().toString();

        return request.party().partyIds().stream()
            .map(partyId -> Message.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withDeliveryId(UUID.randomUUID().toString())
                .withPartyId(partyId)
                .withType(MessageType.LETTER)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build())
            .toList();
    }

    Message toMessage(final String batchId, final MessageRequest.Message request) {
        var messageId = UUID.randomUUID().toString();

        return Message.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(MessageRequest.Message.Party::partyId)
                .orElse(null))
            .withType(MessageType.MESSAGE)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }
}

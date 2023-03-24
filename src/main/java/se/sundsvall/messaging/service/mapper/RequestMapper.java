package se.sundsvall.messaging.service.mapper;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.model.Message;

@Component
public class RequestMapper {

    static final Gson GSON = new GsonBuilder().create();

    private final String defaultSmsRequestSender;
    private final EmailRequest.Sender defaultEmailRequestSender;

    public RequestMapper(final Defaults defaults) {
        defaultSmsRequestSender = defaults.sms().name();

        defaultEmailRequestSender = EmailRequest.Sender.builder()
            .withName(defaults.email().name())
            .withAddress(defaults.email().address())
            .withReplyTo(defaults.email().replyTo())
            .build();
    }

    public String toSmsRequest(final Message message, final String mobileNumber) {
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

    public String toEmailRequest(final Message message, final String emailAddress) {
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

    public DigitalMailRequest toDigitalMailRequest(final LetterRequest request) {
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

    public SnailMailRequest toSnailMailRequest(final LetterRequest request) {
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
}

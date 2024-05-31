package se.sundsvall.messaging.service.mapper;

import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import java.util.List;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.model.Message;

@Component
public class RequestMapper {

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
        var originalMessage = fromJson(message.content(), MessageRequest.Message.class);

        var sender = ofNullable(originalMessage.sender())
            .map(MessageRequest.Message.Sender::sms)
            .map(MessageRequest.Message.Sender.Sms::name)
            .orElse(defaultSmsRequestSender);

        var smsRequest = SmsRequest.builder()
            .withParty(SmsRequest.Party.builder()
                .withPartyId(ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::partyId)
                    .orElse(null))
                .withExternalReferences(ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::externalReferences)
                    .orElse(null))
                .build())
            .withSender(sender)
            .withMobileNumber(mobileNumber)
            .withMessage(originalMessage.message())
            .withOrigin(message.origin())
            .build();

        return toJson(smsRequest);
    }

    public String toEmailRequest(final Message message, final String emailAddress) {
        var originalMessage = fromJson(message.content(), MessageRequest.Message.class);

        var sender = ofNullable(originalMessage.sender())
            .map(MessageRequest.Message.Sender::email)
            .map(emailSender -> EmailRequest.Sender.builder()
                .withName(emailSender.name())
                .withAddress(emailSender.address())
                .withReplyTo(emailSender.replyTo())
                .build())
            .orElse(defaultEmailRequestSender);

        var emailRequest = EmailRequest.builder()
            .withParty(EmailRequest.Party.builder()
                .withPartyId(ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::partyId)
                    .orElse(null))
                .withExternalReferences(ofNullable(originalMessage.party())
                    .map(MessageRequest.Message.Party::externalReferences)
                    .orElse(null))
                .build())
            .withSender(sender)
            .withEmailAddress(emailAddress)
            .withSubject(originalMessage.subject())
            .withMessage(originalMessage.message())
            .withHtmlMessage(originalMessage.htmlMessage())
            .withOrigin(message.origin())
            .build();

        return toJson(emailRequest);
    }

    public DigitalMailRequest toDigitalMailRequest(final LetterRequest request, final String partyId) {
        return DigitalMailRequest.builder()
            .withSender(DigitalMailRequest.Sender.builder()
                .withSupportInfo(ofNullable(request.sender()).map(LetterRequest.Sender::supportInfo)
                    .map(supportInfo -> DigitalMailRequest.Sender.SupportInfo.builder()
                        .withText(supportInfo.text())
                        .withUrl(supportInfo.url())
                        .withEmailAddress(supportInfo.emailAddress())
                        .withPhoneNumber(supportInfo.phoneNumber())
                        .build())
                    .orElse(null))
                .build())
            .withParty(DigitalMailRequest.Party.builder()
                .withPartyIds(List.of(partyId))
                .withExternalReferences(request.party().externalReferences())
                .build())
            .withContentType(request.contentType())
            .withSubject(request.subject())
            .withDepartment(request.department())
            .withBody(request.body())
            .withAttachments(request.attachments().stream()
                .filter(LetterRequest.Attachment::isIntendedForDigitalMail)
                .map(attachment -> DigitalMailRequest.Attachment.builder()
                    .withFilename(attachment.filename())
                    .withContent(attachment.content())
                    .withContentType(attachment.contentType())
                    .build())
                .toList())
            .withOrigin(request.origin())
            .build();
    }

    public SnailMailRequest toSnailMailRequest(final LetterRequest request, final String partyId) {
        return SnailMailRequest.builder()
            .withParty(SnailMailRequest.Party.builder()
                .withPartyId(partyId)
                .build())
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
            .withOrigin(request.origin())
            .build();
    }

	public SmsRequest toSmsRequest(final SmsBatchRequest request, final SmsBatchRequest.Party party) {
		return SmsRequest.builder()
			.withParty(SmsRequest.Party.builder()
				.withPartyId(party.partyId())
				.build())
			.withMessage(request.message())
			.withMobileNumber(party.mobileNumber())
			.withOrigin(request.origin())
			.withSender(request.sender())
			.build();
	}
}

package se.sundsvall.messaging.service.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
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

@Component
public class DtoMapper {

    private final String defaultSmsDtoSender;
    private final EmailDto.Sender defaultEmailDtoSender;
    private final String defaultDigitalMailDtoMunicipalityId;
    private final DigitalMailDto.Sender.SupportInfo defaultDigitalMailDtoSenderSupportInfo;

    public DtoMapper(final Defaults defaults) {
        defaultSmsDtoSender = defaults.sms().name();

        defaultEmailDtoSender = EmailDto.Sender.builder()
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

    public SmsDto toSmsDto(final SmsRequest request) {
        return SmsDto.builder()
            .withSender(Optional.ofNullable(request.sender())
                .orElse(defaultSmsDtoSender))
            .withMobileNumber(request.mobileNumber())
            .withMessage(request.message())
            .build();
    }

    public EmailDto toEmailDto(final EmailRequest request) {
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

    public DigitalMailDto toDigitalMailDto(final DigitalMailRequest request, final String partyId) {
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

    public WebMessageDto toWebMessageDto(final WebMessageRequest request) {
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

    public SnailMailDto toSnailmailDto(final SnailMailRequest request) {
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
}
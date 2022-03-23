package se.sundsvall.messaging.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.model.dto.EmailDto;
import se.sundsvall.messaging.model.entity.EmailEntity;

import generated.se.sundsvall.emailsender.Attachment;
import generated.se.sundsvall.emailsender.EmailRequest;

public final class EmailMapper {

    private EmailMapper() {
    }

    public static EmailEntity toEntity(IncomingEmailRequest request) {
        if (request == null) {
            return null;
        }

        List<EmailEntity.Attachment> attachments = Optional.ofNullable(request.getAttachments())
                .stream()
                .flatMap(Collection::stream)
                .map(attachment -> EmailEntity.Attachment.builder()
                        .withId(UUID.randomUUID().toString())
                        .withContent(attachment.getContent())
                        .withContentType(attachment.getContentType())
                        .withName(attachment.getName())
                        .build())
                .collect(Collectors.toList());

        return EmailEntity.builder()
                .withBatchId(UUID.randomUUID().toString())
                .withMessageId(UUID.randomUUID().toString())
                .withPartyId(request.getPartyId())
                .withEmailAddress(request.getEmailAddress())
                .withSenderName(request.getSenderName())
                .withSenderEmail(request.getSenderEmail())
                .withMessage(request.getMessage())
                .withHtmlMessage(request.getHtmlMessage())
                .withStatus(MessageStatus.PENDING)
                .withSubject(request.getSubject())
                .withAttachments(attachments)
                .build();
    }

    public static EmailDto toDto(EmailEntity entity) {
        if (entity == null) {
            return null;
        }
        List<EmailDto.AttachmentDto> attachments = entity.getAttachments()
                .stream()
                .map(attachment -> EmailDto.AttachmentDto.builder()
                        .withContent(attachment.getContent())
                        .withContentType(attachment.getContentType())
                        .withName(attachment.getName())
                        .build())
                .collect(Collectors.toList());

        return EmailDto.builder()
                .withBatchId(entity.getBatchId())
                .withMessageId(entity.getMessageId())
                .withPartyId(entity.getPartyId())
                .withStatus(entity.getStatus())
                .withMessage(entity.getMessage())
                .withHtmlMessage(entity.getHtmlMessage())
                .withEmailAddress(entity.getEmailAddress())
                .withSenderName(entity.getSenderName())
                .withSenderEmail(entity.getSenderEmail())
                .withSubject(entity.getSubject())
                .withAttachments(attachments)
                .build();
    }

    public static EmailRequest toRequest(EmailEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Attachment> attachments = Optional.ofNullable(entity.getAttachments())
                .stream()
                .flatMap(Collection::stream)
                .map(attachment -> new Attachment()
                        .content(attachment.getContent())
                        .contentType(attachment.getContentType())
                        .name(attachment.getName()))
                .collect(Collectors.toList());

        return new EmailRequest()
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .emailAddress(entity.getEmailAddress())
                .senderName(entity.getSenderName())
                .senderEmail(entity.getSenderEmail())
                .htmlMessage(entity.getHtmlMessage())
                .attachments(attachments);
    }
}

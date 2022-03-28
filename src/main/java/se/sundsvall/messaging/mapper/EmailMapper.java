package se.sundsvall.messaging.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

public final class EmailMapper {

    private EmailMapper() { }

    public static EmailEntity toEntity(final EmailRequest request) {
        if (request == null) {
            return null;
        }

        var attachments = Optional.ofNullable(request.getAttachments()).stream()
            .flatMap(Collection::stream)
            .map(attachment -> EmailEntity.Attachment.builder()
                .withId(UUID.randomUUID().toString())
                .withContent(attachment.getContent())
                .withContentType(attachment.getContentType())
                .withName(attachment.getName())
                .build())
            .toList();

        return EmailEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.getParty()).map(Party::getPartyId).orElse(null))
            .withExternalReferences(Optional.ofNullable(request.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ExternalReference::getKey, ExternalReference::getValue)))
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

    public static EmailDto toDto(final EmailEntity entity) {
        if (entity == null) {
            return null;
        }

        var attachments = entity.getAttachments().stream()
            .map(attachment -> EmailDto.AttachmentDto.builder()
                .withContent(attachment.getContent())
                .withContentType(attachment.getContentType())
                .withName(attachment.getName())
                .build())
            .toList();

        return EmailDto.builder()
            .withBatchId(entity.getBatchId())
            .withMessageId(entity.getMessageId())
            .withParty(Party.builder()
                .withPartyId(entity.getPartyId())
                .withExternalReferences(Optional.ofNullable(entity.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
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
}

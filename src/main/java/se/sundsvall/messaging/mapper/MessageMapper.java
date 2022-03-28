package se.sundsvall.messaging.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.request.MessageRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

public final class MessageMapper {

    private MessageMapper() { }

    public static MessageBatchDto toMessageBatch(final List<MessageEntity> entities) {
        if (entities == null) {
            return null;
        }

        var batchId = entities.stream()
            .findFirst()
            .map(MessageEntity::getBatchId);

        if (entities.isEmpty() || batchId.isEmpty()) {
            return MessageBatchDto.builder()
                .withMessages(Collections.emptyList())
                .build();
        }

        var messages = entities.stream()
            .map(MessageMapper::toDto)
            .collect(Collectors.toList());

        return MessageBatchDto.builder()
            .withBatchId(batchId.get())
            .withMessages(messages)
            .build();
    }

    public static MessageBatchDto toMessageBatch(final MessageRequest messageRequest) {
        if (messageRequest == null || messageRequest.getMessages() == null) {
            return null;
        }

        var messages = messageRequest.getMessages().stream()
            .map(message -> MessageBatchDto.Message.builder()
                .withParty(message.getParty())
                .withMessageId(UUID.randomUUID().toString())
                .withMessage(message.getMessage())
                .withEmailName(message.getEmailName())
                .withSmsName(message.getSmsName())
                .withSubject(message.getSubject())
                .withSenderEmail(message.getSenderEmail())
                .build())
            .collect(Collectors.toList());

        return MessageBatchDto.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessages(messages)
            .build();
    }

    public static MessageEntity toEntity(final MessageBatchDto.Message message, final String batchId) {
        if (message == null) {
            return null;
        }

        return MessageEntity.builder()
            .withBatchId(batchId)
            .withMessageId(UUID.randomUUID().toString())
            .withMessage(message.getMessage())
            .withSenderEmail(message.getSenderEmail())
            .withSmsName(message.getSmsName())
            .withEmailName(message.getEmailName())
            .withPartyId(Optional.ofNullable(message.getParty()).map(Party::getPartyId).orElse(null))
            .withExternalReferences(Optional.ofNullable(message.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ExternalReference::getKey, ExternalReference::getValue)))
            .withSubject(message.getSubject())
            .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
            .build();
    }

    public static MessageBatchDto.Message toDto(final MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return MessageBatchDto.Message.builder()
            .withMessageId(entity.getMessageId())
            .withMessage(entity.getMessage())
            .withSmsName(entity.getSmsName())
            .withEmailName(entity.getEmailName())
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
            .withSubject(entity.getSubject())
            .withSenderEmail(entity.getSenderEmail())
            .build();
    }
}

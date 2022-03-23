package se.sundsvall.messaging.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.MessageRequest;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.entity.MessageEntity;

public final class MessageMapper {

    private MessageMapper() {
    }

    public static MessageBatchDto toMessageBatch(List<MessageEntity> entities) {
        if (entities == null) {
            return null;
        }

        Optional<String> batchId = entities.stream()
                .findFirst()
                .map(MessageEntity::getBatchId);

        if (entities.isEmpty() || batchId.isEmpty()) {
            return MessageBatchDto.builder()
                    .withMessages(Collections.emptyList())
                    .build();
        }

        List<MessageBatchDto.Message> messages = entities.stream()
                .map(MessageMapper::toDto)
                .collect(Collectors.toList());

        return MessageBatchDto.builder()
                .withBatchId(batchId.get())
                .withMessages(messages)
                .build();
    }

    public static MessageBatchDto toMessageBatch(MessageRequest messageRequest) {
        if (messageRequest == null || messageRequest.getMessages() == null) {
            return null;
        }

        List<MessageBatchDto.Message> messages = messageRequest.getMessages()
                .stream()
                .map(message -> MessageBatchDto.Message.builder()
                        .withPartyId(message.getPartyId())
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

    public static MessageEntity toEntity(MessageBatchDto.Message message, String batchId) {
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
                .withPartyId(message.getPartyId())
                .withSubject(message.getSubject())
                .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
                .build();
    }

    public static MessageBatchDto.Message toDto(MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        return MessageBatchDto.Message.builder()
                .withMessageId(entity.getMessageId())
                .withMessage(entity.getMessage())
                .withSmsName(entity.getSmsName())
                .withEmailName(entity.getEmailName())
                .withPartyId(entity.getPartyId())
                .withSubject(entity.getSubject())
                .withSenderEmail(entity.getSenderEmail())
                .build();
    }
}

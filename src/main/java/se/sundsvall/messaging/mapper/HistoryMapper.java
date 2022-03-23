package se.sundsvall.messaging.mapper;

import java.time.LocalDateTime;
import java.util.UUID;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.model.dto.HistoryDto;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.model.entity.HistoryEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;

public final class HistoryMapper {

    private HistoryMapper() {
    }

    public static HistoryEntity toHistory(EmailEntity email) {
        if (email == null) {
            return null;
        }
        return HistoryEntity.builder()
                .withId(UUID.randomUUID().toString())
                .withBatchId(email.getBatchId())
                .withMessageId(email.getMessageId())
                .withMessage(email.getMessage())
                .withSender(email.getSenderName())
                .withStatus(MessageStatus.SENT)
                .withPartyId(email.getPartyId())
                .withMessageType(MessageType.EMAIL)
                .withPartyContact(email.getEmailAddress())
                .withCreatedAt(LocalDateTime.now())
                .build();
    }

    public static HistoryEntity toHistory(SmsEntity sms) {
        if (sms == null) {
            return null;
        }
        return HistoryEntity.builder()
                .withId(UUID.randomUUID().toString())
                .withBatchId(sms.getBatchId())
                .withMessageId(sms.getMessageId())
                .withMessage(sms.getMessage())
                .withSender(sms.getSender())
                .withStatus(MessageStatus.SENT)
                .withPartyId(sms.getPartyId())
                .withMessageType(MessageType.SMS)
                .withPartyContact(sms.getMobileNumber())
                .withCreatedAt(LocalDateTime.now())
                .build();
    }

    public static HistoryEntity toHistory(UndeliverableMessageDto undeliverable) {
        if (undeliverable == null) {
            return null;
        }
        return HistoryEntity.builder()
                .withId(UUID.randomUUID().toString())
                .withBatchId(undeliverable.getBatchId())
                .withMessageId(undeliverable.getMessageId())
                .withPartyId(undeliverable.getPartyId())
                .withPartyContact(undeliverable.getPartyContact())
                .withSender(undeliverable.getSenderName())
                .withMessage(undeliverable.getContent())
                .withStatus(undeliverable.getStatus())
                .withMessageType(undeliverable.getType())
                .withCreatedAt(LocalDateTime.now())
                .build();
    }

    public static HistoryDto toHistoryDto(HistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return HistoryDto.builder()
                .withId(entity.getId())
                .withBatchId(entity.getBatchId())
                .withMessageId(entity.getMessageId())
                .withMessage(entity.getMessage())
                .withSender(entity.getSender())
                .withStatus(entity.getStatus())
                .withPartyId(entity.getPartyId())
                .withMessageType(entity.getMessageType())
                .withPartyContact(entity.getPartyContact())
                .withCreatedAt(entity.getCreatedAt())
                .build();
    }
}

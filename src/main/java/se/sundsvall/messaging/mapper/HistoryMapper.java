package se.sundsvall.messaging.mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

public final class HistoryMapper {

    private HistoryMapper() { }

    public static HistoryEntity toHistory(final EmailEntity email) {
        if (email == null) {
            return null;
        }

        return HistoryEntity.builder()
            .withBatchId(email.getBatchId())
            .withMessageId(email.getMessageId())
            .withMessage(email.getMessage())
            .withSender(email.getSenderName())
            .withStatus(MessageStatus.SENT)
            .withPartyId(email.getPartyId())
            .withExternalReferences(email.getExternalReferences())
            .withMessageType(MessageType.EMAIL)
            .withPartyContact(email.getEmailAddress())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }

    public static HistoryEntity toHistory(final SmsEntity sms) {
        if (sms == null) {
            return null;
        }

        return HistoryEntity.builder()
            .withBatchId(sms.getBatchId())
            .withMessageId(sms.getMessageId())
            .withMessage(sms.getMessage())
            .withSender(sms.getSender())
            .withStatus(MessageStatus.SENT)
            .withPartyId(sms.getPartyId())
            .withExternalReferences(sms.getExternalReferences())
            .withMessageType(MessageType.SMS)
            .withPartyContact(sms.getMobileNumber())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }

    public static HistoryEntity toHistory(final UndeliverableMessageDto undeliverable) {
        if (undeliverable == null) {
            return null;
        }

        return HistoryEntity.builder()
            .withBatchId(undeliverable.getBatchId())
            .withMessageId(undeliverable.getMessageId())
            .withPartyId(Optional.ofNullable(undeliverable.getParty()).map(Party::getPartyId).orElse(null))
            .withExternalReferences(Optional.ofNullable(undeliverable.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ExternalReference::getKey, ExternalReference::getValue)))
            .withPartyContact(undeliverable.getPartyContact())
            .withSender(undeliverable.getSenderName())
            .withMessage(undeliverable.getContent())
            .withStatus(undeliverable.getStatus())
            .withMessageType(undeliverable.getType())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }

    public static HistoryDto toHistoryDto(final HistoryEntity entity) {
        if (entity == null) {
            return null;
        }

        return HistoryDto.builder()
            .withBatchId(entity.getBatchId())
            .withMessageId(entity.getMessageId())
            .withMessage(entity.getMessage())
            .withSender(entity.getSender())
            .withStatus(entity.getStatus())
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
            .withMessageType(entity.getMessageType())
            .withPartyContact(entity.getPartyContact())
            .withCreatedAt(entity.getCreatedAt())
            .build();
    }
}

package se.sundsvall.messaging.mapper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

public final class UndeliverableMapper {

    private UndeliverableMapper() { }

    /**
     * Maps an incoming message to an undeliverable message, the default status
     * will be set to {@link MessageStatus#FAILED}. It's up to the caller to change
     * to another wanted status after calling this method.
     */
    public static UndeliverableMessageDto toUndeliverable(final MessageEntity message) {
        if (message == null) {
            return null;
        }

        // Type will be null if message was never moved from incoming
        // and not able to be determined by the contact method in feedback settings
        var messageType = message.getMessageType() == null ?
                MessageType.UNDELIVERABLE : message.getMessageType();

        return UndeliverableMessageDto.builder()
            .withBatchId(message.getBatchId())
            .withMessageId(message.getMessageId())
            .withParty(Party.builder()
                .withPartyId(message.getPartyId())
                .withExternalReferences(Optional.ofNullable(message.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
            // No information is available for the receiver (e-mail and mobile number)
            .withPartyContact("")
            .withSubject(message.getSubject())
            .withContent(message.getMessage())
            // Default to email name since it's more descriptive
            .withSenderName(message.getEmailName())
            .withSenderEmail(message.getSenderEmail())
            .withStatus(MessageStatus.FAILED)
            .withType(messageType)
            .build();
    }

    /**
     * Maps an {@code E-mail} to an undeliverable message, the default status
     * will be set to {@link MessageStatus#FAILED}. It's up to the caller to change
     * to another wanted status after calling this method.
     */
    public static UndeliverableMessageDto toUndeliverable(final EmailEntity emailEntity) {
        if (emailEntity == null) {
            return null;
        }

        return UndeliverableMessageDto.builder()
            .withBatchId(emailEntity.getBatchId())
            .withMessageId(emailEntity.getMessageId())
            .withParty(Party.builder()
                .withPartyId(emailEntity.getPartyId())
                .withExternalReferences(Optional.ofNullable(emailEntity.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
            .withPartyContact(emailEntity.getEmailAddress())
            .withSubject(emailEntity.getSubject())
            .withContent(emailEntity.getMessage())
            .withSenderName(emailEntity.getSenderName())
            .withSenderEmail(emailEntity.getSenderEmail())
            .withStatus(MessageStatus.FAILED)
            .withType(MessageType.EMAIL)
            .build();
    }

    /**
     * Maps an {@code SMS} to an undeliverable message, the default status
     * will be set to {@link MessageStatus#FAILED}. It's up to the caller to change
     * to another wanted status after calling this method.
     */
    public static UndeliverableMessageDto toUndeliverable(final SmsEntity smsEntity) {
        if (smsEntity == null) {
            return null;
        }

        return UndeliverableMessageDto.builder()
            .withBatchId(smsEntity.getBatchId())
            .withMessageId(smsEntity.getMessageId())
            .withParty(Party.builder()
                .withPartyId(smsEntity.getPartyId())
                .withExternalReferences(Optional.ofNullable(smsEntity.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
            .withPartyContact(smsEntity.getMobileNumber())
            .withSubject(null)
            .withContent(smsEntity.getMessage())
            .withSenderName(smsEntity.getSender())
            .withSenderEmail(null)
            .withStatus(MessageStatus.FAILED)
            .withType(MessageType.SMS)
            .build();
    }
}

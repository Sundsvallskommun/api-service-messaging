package se.sundsvall.messaging.mapper;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.model.entity.MessageEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;

public final class UndeliverableMapper {

    private UndeliverableMapper() {
    }

    /**
     * Maps an incoming message to an undeliverable message, the default status
     * will be set to {@link MessageStatus#FAILED}. It's up to the caller to change
     * to another wanted status after calling this method.
     */
    public static UndeliverableMessageDto toUndeliverable(MessageEntity message) {
        if (message == null) {
            return null;
        }

        // Type will be null if message was never moved from incoming
        // and not able to be determined by the contact method in feedback settings
        MessageType messageType = message.getMessageType() == null
                ? MessageType.UNDELIVERABLE
                : message.getMessageType();

        return UndeliverableMessageDto.builder()
                .withBatchId(message.getBatchId())
                .withMessageId(message.getMessageId())
                .withPartyId(message.getPartyId())
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
    public static UndeliverableMessageDto toUndeliverable(EmailEntity email) {
        if (email == null) {
            return null;
        }

        return UndeliverableMessageDto.builder()
                .withBatchId(email.getBatchId())
                .withMessageId(email.getMessageId())
                .withPartyId(email.getPartyId())
                .withPartyContact(email.getEmailAddress())
                .withSubject(email.getSubject())
                .withContent(email.getMessage())
                .withSenderName(email.getSenderName())
                .withSenderEmail(email.getSenderEmail())
                .withStatus(MessageStatus.FAILED)
                .withType(MessageType.EMAIL)
                .build();
    }

    /**
     * Maps an {@code SMS} to an undeliverable message, the default status
     * will be set to {@link MessageStatus#FAILED}. It's up to the caller to change
     * to another wanted status after calling this method.
     */
    public static UndeliverableMessageDto toUndeliverable(SmsEntity sms) {
        if (sms == null) {
            return null;
        }

        return UndeliverableMessageDto.builder()
                .withBatchId(sms.getBatchId())
                .withMessageId(sms.getMessageId())
                .withPartyId(sms.getPartyId())
                .withPartyContact(sms.getMobileNumber())
                .withSubject(null)
                .withContent(sms.getMessage())
                .withSenderName(sms.getSender())
                .withSenderEmail(null)
                .withStatus(MessageStatus.FAILED)
                .withType(MessageType.SMS)
                .build();
    }
}

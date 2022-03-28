package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class UndeliverableMapperTests {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();
    private static final String PARTY_ID = UUID.randomUUID().toString();

    @Test
    void toUndeliverable_givenNullIncomingMessage_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((MessageEntity) null)).isNull();
    }

    @Test
    void toUndeliverable_givenIncomingMessage_thenUndeliverableWithSameValues() {
        var messageEntity = MessageEntity.builder()
            .withBatchId(BATCH_ID)
            .withMessageId(MESSAGE_ID)
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withSubject("subject")
            .withMessage("message")
            .withMessageType(MessageType.EMAIL)
            .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
            .withSmsName("sms name")
            .withEmailName("email name")
            .withSenderEmail("noreply@sundsvall.se")
            .build();

        var undeliverableMessageDto = UndeliverableMapper.toUndeliverable(messageEntity);

        assertThat(undeliverableMessageDto.getBatchId()).isEqualTo(messageEntity.getBatchId());
        assertThat(undeliverableMessageDto.getMessageId()).isEqualTo(messageEntity.getMessageId());
        assertThat(undeliverableMessageDto.getParty().getPartyId()).isEqualTo(messageEntity.getPartyId());
        assertThat(undeliverableMessageDto.getParty().getExternalReferences()).hasSameSizeAs(messageEntity.getExternalReferences().entrySet());
        assertThat(undeliverableMessageDto.getPartyContact()).isBlank();
        assertThat(undeliverableMessageDto.getSubject()).isEqualTo(messageEntity.getSubject());
        assertThat(undeliverableMessageDto.getContent()).isEqualTo(messageEntity.getMessage());
        assertThat(undeliverableMessageDto.getSenderName()).isEqualTo(messageEntity.getEmailName());
        assertThat(undeliverableMessageDto.getSenderEmail()).isEqualTo(messageEntity.getSenderEmail());
        assertThat(undeliverableMessageDto.getType()).isEqualTo(MessageType.EMAIL);
    }

    @Test
    void toUndeliverable_givenIncomingMessageWithoutType_thenUndeliverableWithTypeUndeliverable() {
        var messageEntity = MessageEntity.builder().build();

        assertThat(UndeliverableMapper.toUndeliverable(messageEntity).getType())
            .isEqualTo(MessageType.UNDELIVERABLE);
    }

    @Test
    void toUndeliverable_givenAnyMessage_thenChangesStatusToFailed() {
        var messageEntity = MessageEntity.builder()
            .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
            .build();

        var emailEntity = EmailEntity.builder()
            .withStatus(MessageStatus.PENDING)
            .build();

        var smsEntity = SmsEntity.builder()
            .withStatus(MessageStatus.PENDING)
            .build();

        assertThat(UndeliverableMapper.toUndeliverable(messageEntity).getStatus())
            .isEqualTo(MessageStatus.FAILED);

        assertThat(UndeliverableMapper.toUndeliverable(emailEntity).getStatus())
            .isEqualTo(MessageStatus.FAILED);

        assertThat(UndeliverableMapper.toUndeliverable(smsEntity).getStatus())
            .isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenSmsEntity_thenUndeliverableWithStatus_FAILED() {
        var smsEntity = SmsEntity.builder()
            .withBatchId(BATCH_ID)
            .withMessageId(MESSAGE_ID)
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withMessage("message")
            .withSender("sender name")
            .withMobileNumber("+46701234567")
            .withStatus(MessageStatus.PENDING)
            .build();

        var undeliverableMessageDto = UndeliverableMapper.toUndeliverable(smsEntity);

        assertThat(undeliverableMessageDto.getBatchId()).isEqualTo(smsEntity.getBatchId());
        assertThat(undeliverableMessageDto.getMessageId()).isEqualTo(smsEntity.getMessageId());
        assertThat(undeliverableMessageDto.getParty().getPartyId()).isEqualTo(smsEntity.getPartyId());
        assertThat(undeliverableMessageDto.getParty().getExternalReferences())
            .hasSameSizeAs(smsEntity.getExternalReferences().entrySet());
        assertThat(undeliverableMessageDto.getPartyContact()).isEqualTo(smsEntity.getMobileNumber());
        assertThat(undeliverableMessageDto.getSubject()).isNull();
        assertThat(undeliverableMessageDto.getContent()).isEqualTo(smsEntity.getMessage());
        assertThat(undeliverableMessageDto.getSenderName()).isEqualTo(smsEntity.getSender());
        assertThat(undeliverableMessageDto.getSenderEmail()).isNull();
        assertThat(undeliverableMessageDto.getType()).isEqualTo(MessageType.SMS);
        assertThat(undeliverableMessageDto.getStatus()).isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenNullSmsEntity_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((SmsEntity) null)).isNull();
    }

    @Test
    void toUndeliverable_givenEmailEntity_thenUndeliverableWithStatus_FAILED() {
        var emailEntity = EmailEntity.builder()
            .withBatchId(BATCH_ID)
            .withMessageId(MESSAGE_ID)
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withEmailAddress("john.doe@example.com")
            .withSubject("subject")
            .withMessage("message")
            .withHtmlMessage("<p>html message</p>")
            .withSenderName("sender name")
            .withSenderEmail("noreply@sundsvall.se")
            .withStatus(MessageStatus.PENDING)
            .build();

        var undeliverableMessageDto = UndeliverableMapper.toUndeliverable(emailEntity);

        assertThat(undeliverableMessageDto.getBatchId()).isEqualTo(emailEntity.getBatchId());
        assertThat(undeliverableMessageDto.getMessageId()).isEqualTo(emailEntity.getMessageId());
        assertThat(undeliverableMessageDto.getParty().getPartyId()).isEqualTo(emailEntity.getPartyId());
        assertThat(undeliverableMessageDto.getParty().getExternalReferences())
            .hasSameSizeAs(emailEntity.getExternalReferences().entrySet());
        assertThat(undeliverableMessageDto.getPartyContact()).isEqualTo(emailEntity.getEmailAddress());
        assertThat(undeliverableMessageDto.getSubject()).isEqualTo(emailEntity.getSubject());
        assertThat(undeliverableMessageDto.getContent()).isEqualTo(emailEntity.getMessage());
        assertThat(undeliverableMessageDto.getSenderName()).isEqualTo(emailEntity.getSenderName());
        assertThat(undeliverableMessageDto.getSenderEmail()).isEqualTo(emailEntity.getSenderEmail());
        assertThat(undeliverableMessageDto.getType()).isEqualTo(MessageType.EMAIL);
        assertThat(undeliverableMessageDto.getStatus()).isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenNullEmailEntity_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((EmailEntity) null)).isNull();
    }
}

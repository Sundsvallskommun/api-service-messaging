package se.sundsvall.messaging.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.model.entity.MessageEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;

class UndeliverableMapperTest {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();
    private static final String PARTY_ID = UUID.randomUUID().toString();

    @Test
    void toUndeliverable_givenNullIncomingMessage_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((MessageEntity) null)).isNull();
    }

    @Test
    void toUndeliverable_givenIncomingMessage_thenUndeliverableWithSameValues() {
        MessageEntity message = MessageEntity.builder()
                .withBatchId(BATCH_ID)
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
                .withSubject("subject")
                .withMessage("message")
                .withMessageType(MessageType.EMAIL)
                .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
                .withSmsName("sms name")
                .withEmailName("email name")
                .withSenderEmail("noreply@sundsvall.se")
                .build();

        UndeliverableMessageDto undeliverable = UndeliverableMapper.toUndeliverable(message);

        assertThat(undeliverable.getBatchId()).isEqualTo(message.getBatchId());
        assertThat(undeliverable.getMessageId()).isEqualTo(message.getMessageId());
        assertThat(undeliverable.getPartyId()).isEqualTo(message.getPartyId());
        assertThat(undeliverable.getPartyContact()).isBlank();
        assertThat(undeliverable.getSubject()).isEqualTo(message.getSubject());
        assertThat(undeliverable.getContent()).isEqualTo(message.getMessage());
        assertThat(undeliverable.getSenderName()).isEqualTo(message.getEmailName());
        assertThat(undeliverable.getSenderEmail()).isEqualTo(message.getSenderEmail());
        assertThat(undeliverable.getType()).isEqualTo(MessageType.EMAIL);
    }

    @Test
    void toUndeliverable_givenIncomingMessageWithoutType_thenUndeliverableWithTypeUndeliverable() {
        MessageEntity incomingMessage = MessageEntity.builder().build();

        assertThat(UndeliverableMapper.toUndeliverable(incomingMessage).getType())
                .isEqualTo(MessageType.UNDELIVERABLE);
    }

    @Test
    void toUndeliverable_givenAnyMessage_thenChangesStatusToFailed() {
        MessageEntity incoming = MessageEntity.builder()
                .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
                .build();

        EmailEntity email = EmailEntity.builder()
                .withStatus(MessageStatus.PENDING)
                .build();

        SmsEntity sms = SmsEntity.builder()
                .withStatus(MessageStatus.PENDING)
                .build();

        assertThat(UndeliverableMapper.toUndeliverable(incoming).getStatus())
                .isEqualTo(MessageStatus.FAILED);

        assertThat(UndeliverableMapper.toUndeliverable(email).getStatus())
                .isEqualTo(MessageStatus.FAILED);

        assertThat(UndeliverableMapper.toUndeliverable(sms).getStatus())
                .isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenSmsEntity_thenUndeliverableWithStatus_FAILED() {
        SmsEntity message = SmsEntity.builder()
                .withBatchId(BATCH_ID)
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
                .withMessage("message")
                .withSender("sender name")
                .withMobileNumber("+46701234567")
                .withStatus(MessageStatus.PENDING)
                .build();

        UndeliverableMessageDto undeliverable = UndeliverableMapper.toUndeliverable(message);

        assertThat(undeliverable.getBatchId()).isEqualTo(message.getBatchId());
        assertThat(undeliverable.getMessageId()).isEqualTo(message.getMessageId());
        assertThat(undeliverable.getPartyId()).isEqualTo(message.getPartyId());
        assertThat(undeliverable.getPartyContact()).isEqualTo(message.getMobileNumber());
        assertThat(undeliverable.getSubject()).isNull();
        assertThat(undeliverable.getContent()).isEqualTo(message.getMessage());
        assertThat(undeliverable.getSenderName()).isEqualTo(message.getSender());
        assertThat(undeliverable.getSenderEmail()).isNull();
        assertThat(undeliverable.getType()).isEqualTo(MessageType.SMS);
        assertThat(undeliverable.getStatus()).isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenNullSmsEntity_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((SmsEntity) null)).isNull();
    }

    @Test
    void toUndeliverable_givenEmailEntity_thenUndeliverableWithStatus_FAILED() {
        EmailEntity message = EmailEntity.builder()
                .withBatchId(BATCH_ID)
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
                .withEmailAddress("john.doe@example.com")
                .withSubject("subject")
                .withMessage("message")
                .withHtmlMessage("<p>html message</p>")
                .withSenderName("sender name")
                .withSenderEmail("noreply@sundsvall.se")
                .withStatus(MessageStatus.PENDING)
                .build();

        UndeliverableMessageDto undeliverable = UndeliverableMapper.toUndeliverable(message);

        assertThat(undeliverable.getBatchId()).isEqualTo(message.getBatchId());
        assertThat(undeliverable.getMessageId()).isEqualTo(message.getMessageId());
        assertThat(undeliverable.getPartyId()).isEqualTo(message.getPartyId());
        assertThat(undeliverable.getPartyContact()).isEqualTo(message.getEmailAddress());
        assertThat(undeliverable.getSubject()).isEqualTo(message.getSubject());
        assertThat(undeliverable.getContent()).isEqualTo(message.getMessage());
        assertThat(undeliverable.getSenderName()).isEqualTo(message.getSenderName());
        assertThat(undeliverable.getSenderEmail()).isEqualTo(message.getSenderEmail());
        assertThat(undeliverable.getType()).isEqualTo(MessageType.EMAIL);
        assertThat(undeliverable.getStatus()).isEqualTo(MessageStatus.FAILED);
    }

    @Test
    void toUndeliverable_givenNullEmailEntity_thenNullUndeliverable() {
        assertThat(UndeliverableMapper.toUndeliverable((EmailEntity) null)).isNull();
    }
}

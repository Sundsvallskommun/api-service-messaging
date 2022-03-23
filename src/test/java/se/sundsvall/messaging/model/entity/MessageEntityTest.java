package se.sundsvall.messaging.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

class MessageEntityTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        MessageEntity message = MessageEntity.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withSmsName("Sundsvall")
                .withSenderEmail("noreply@sundsvall.se")
                .withEmailName("Sundsvalls kommun")
                .withSubject("subject")
                .withMessage("message")
                .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
                .withMessageType(MessageType.EMAIL)
                .build();

        assertThat(message.getBatchId()).isEqualTo(batchId);
        assertThat(message.getMessageId()).isEqualTo(messageId);
        assertThat(message.getPartyId()).isEqualTo(partyId);
        assertThat(message.getSmsName()).isEqualTo("Sundsvall");
        assertThat(message.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(message.getEmailName()).isEqualTo("Sundsvalls kommun");
        assertThat(message.getSubject()).isEqualTo("subject");
        assertThat(message.getMessage()).isEqualTo("message");
        assertThat(message.getMessageStatus()).isEqualTo(MessageStatus.AWAITING_FEEDBACK);
        assertThat(message.getMessageType()).isEqualTo(MessageType.EMAIL);
    }

    @Test
    void testSetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        MessageEntity message = new MessageEntity();
        message.setBatchId(batchId);
        message.setMessageId(messageId);
        message.setPartyId(partyId);
        message.setSmsName("Sundsvall");
        message.setSenderEmail("noreply@sundsvall.se");
        message.setEmailName("Sundsvalls kommun");
        message.setSubject("subject");
        message.setMessage("message");
        message.setMessageStatus(MessageStatus.AWAITING_FEEDBACK);
        message.setMessageType(MessageType.EMAIL);

        assertThat(message.getBatchId()).isEqualTo(batchId);
        assertThat(message.getMessageId()).isEqualTo(messageId);
        assertThat(message.getPartyId()).isEqualTo(partyId);
        assertThat(message.getSmsName()).isEqualTo("Sundsvall");
        assertThat(message.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(message.getEmailName()).isEqualTo("Sundsvalls kommun");
        assertThat(message.getSubject()).isEqualTo("subject");
        assertThat(message.getMessage()).isEqualTo("message");
        assertThat(message.getMessageStatus()).isEqualTo(MessageStatus.AWAITING_FEEDBACK);
        assertThat(message.getMessageType()).isEqualTo(MessageType.EMAIL);
    }

}

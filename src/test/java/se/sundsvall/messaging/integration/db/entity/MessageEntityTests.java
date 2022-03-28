package se.sundsvall.messaging.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class MessageEntityTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();

        var messageEntity = MessageEntity.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withPartyId(partyId)
            .withExternalReferences(Map.of("key", "value"))
            .withSmsName("Sundsvall")
            .withSenderEmail("noreply@sundsvall.se")
            .withEmailName("Sundsvalls kommun")
            .withSubject("subject")
            .withMessage("message")
            .withMessageStatus(MessageStatus.AWAITING_FEEDBACK)
            .withMessageType(MessageType.EMAIL)
            .build();

        assertThat(messageEntity.getBatchId()).isEqualTo(batchId);
        assertThat(messageEntity.getMessageId()).isEqualTo(messageId);
        assertThat(messageEntity.getPartyId()).isEqualTo(partyId);
        assertThat(messageEntity.getSmsName()).isEqualTo("Sundsvall");
        assertThat(messageEntity.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(messageEntity.getEmailName()).isEqualTo("Sundsvalls kommun");
        assertThat(messageEntity.getSubject()).isEqualTo("subject");
        assertThat(messageEntity.getMessage()).isEqualTo("message");
        assertThat(messageEntity.getMessageStatus()).isEqualTo(MessageStatus.AWAITING_FEEDBACK);
        assertThat(messageEntity.getMessageType()).isEqualTo(MessageType.EMAIL);
    }

    @Test
    void testSetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();

        var messageEntity = new MessageEntity();
        messageEntity.setBatchId(batchId);
        messageEntity.setMessageId(messageId);
        messageEntity.setPartyId(partyId);
        messageEntity.setSmsName("Sundsvall");
        messageEntity.setSenderEmail("noreply@sundsvall.se");
        messageEntity.setEmailName("Sundsvalls kommun");
        messageEntity.setSubject("subject");
        messageEntity.setMessage("message");
        messageEntity.setMessageStatus(MessageStatus.AWAITING_FEEDBACK);
        messageEntity.setMessageType(MessageType.EMAIL);

        assertThat(messageEntity.getBatchId()).isEqualTo(batchId);
        assertThat(messageEntity.getMessageId()).isEqualTo(messageId);
        assertThat(messageEntity.getPartyId()).isEqualTo(partyId);
        assertThat(messageEntity.getSmsName()).isEqualTo("Sundsvall");
        assertThat(messageEntity.getSenderEmail()).isEqualTo("noreply@sundsvall.se");
        assertThat(messageEntity.getEmailName()).isEqualTo("Sundsvalls kommun");
        assertThat(messageEntity.getSubject()).isEqualTo("subject");
        assertThat(messageEntity.getMessage()).isEqualTo("message");
        assertThat(messageEntity.getMessageStatus()).isEqualTo(MessageStatus.AWAITING_FEEDBACK);
        assertThat(messageEntity.getMessageType()).isEqualTo(MessageType.EMAIL);
    }

}

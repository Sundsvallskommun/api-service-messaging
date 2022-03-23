package se.sundsvall.messaging.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

class HistoryEntityTest {

    @Test
    void testBuilderAndGetters() {
        String id = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        HistoryEntity history = HistoryEntity.builder()
                .withId(id)
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withMessage("message")
                .withPartyContact("john.doe@example.com")
                .withSender("Sundsvalls kommun")
                .withMessageType(MessageType.EMAIL)
                .withStatus(MessageStatus.SENT)
                .withCreatedAt(dateTimeNow)
                .build();

        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getBatchId()).isEqualTo(batchId);
        assertThat(history.getMessageId()).isEqualTo(messageId);
        assertThat(history.getPartyId()).isEqualTo(partyId);
        assertThat(history.getMessage()).isEqualTo("message");
        assertThat(history.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(history.getSender()).isEqualTo("Sundsvalls kommun");
        assertThat(history.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(history.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(history.getCreatedAt()).isEqualTo(dateTimeNow);
    }

    @Test
    void testSetters() {
        String id = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        HistoryEntity history = new HistoryEntity();
        history.setId(id);
        history.setBatchId(batchId);
        history.setMessageId(messageId);
        history.setPartyId(partyId);
        history.setMessage("message");
        history.setPartyContact("john.doe@example.com");
        history.setSender("Sundsvalls kommun");
        history.setMessageType(MessageType.EMAIL);
        history.setStatus(MessageStatus.SENT);
        history.setCreatedAt(dateTimeNow);

        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getBatchId()).isEqualTo(batchId);
        assertThat(history.getMessageId()).isEqualTo(messageId);
        assertThat(history.getPartyId()).isEqualTo(partyId);
        assertThat(history.getMessage()).isEqualTo("message");
        assertThat(history.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(history.getSender()).isEqualTo("Sundsvalls kommun");
        assertThat(history.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(history.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(history.getCreatedAt()).isEqualTo(dateTimeNow);
    }

}

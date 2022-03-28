package se.sundsvall.messaging.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class HistoryEntityTests {

    @Test
    void testBuilderAndGetters() {
        var id = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var historyEntity = HistoryEntity.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withPartyId(partyId)
            .withExternalReferences(Map.of("key", "value"))
            .withMessage("message")
            .withPartyContact("john.doe@example.com")
            .withSender("Sundsvalls kommun")
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.SENT)
            .withCreatedAt(now)
            .build();

        assertThat(historyEntity.getBatchId()).isEqualTo(batchId);
        assertThat(historyEntity.getMessageId()).isEqualTo(messageId);
        assertThat(historyEntity.getPartyId()).isEqualTo(partyId);
        assertThat(historyEntity.getExternalReferences()).hasSize(1);
        assertThat(historyEntity.getMessage()).isEqualTo("message");
        assertThat(historyEntity.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(historyEntity.getSender()).isEqualTo("Sundsvalls kommun");
        assertThat(historyEntity.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(historyEntity.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(historyEntity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testSetters() {
        var id = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var historyEntity = new HistoryEntity();
        historyEntity.setBatchId(batchId);
        historyEntity.setMessageId(messageId);
        historyEntity.setPartyId(partyId);
        historyEntity.setExternalReferences(Map.of("key", "value"));
        historyEntity.setMessage("message");
        historyEntity.setPartyContact("john.doe@example.com");
        historyEntity.setSender("Sundsvalls kommun");
        historyEntity.setMessageType(MessageType.EMAIL);
        historyEntity.setStatus(MessageStatus.SENT);
        historyEntity.setCreatedAt(now);

        assertThat(historyEntity.getBatchId()).isEqualTo(batchId);
        assertThat(historyEntity.getMessageId()).isEqualTo(messageId);
        assertThat(historyEntity.getPartyId()).isEqualTo(partyId);
        assertThat(historyEntity.getExternalReferences()).hasSize(1);
        assertThat(historyEntity.getMessage()).isEqualTo("message");
        assertThat(historyEntity.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(historyEntity.getSender()).isEqualTo("Sundsvalls kommun");
        assertThat(historyEntity.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(historyEntity.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(historyEntity.getCreatedAt()).isEqualTo(now);
    }

}

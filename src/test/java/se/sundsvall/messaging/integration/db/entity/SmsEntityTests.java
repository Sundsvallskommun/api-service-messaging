package se.sundsvall.messaging.integration.db.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;

class SmsEntityTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var smsEntity = SmsEntity.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withPartyId(partyId)
            .withSender("Sundsvall")
            .withMessage("message")
            .withMobileNumber("+46701234567")
            .withStatus(MessageStatus.PENDING)
            .withCreatedAt(now)
            .build();

        assertThat(smsEntity.getBatchId()).isEqualTo(batchId);
        assertThat(smsEntity.getMessageId()).isEqualTo(messageId);
        assertThat(smsEntity.getPartyId()).isEqualTo(partyId);
        assertThat(smsEntity.getSender()).isEqualTo("Sundsvall");
        assertThat(smsEntity.getMessage()).isEqualTo("message");
        assertThat(smsEntity.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(smsEntity.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(smsEntity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void testSetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var dateTimeNow = LocalDateTime.now();

        var smsEntity = new SmsEntity();
        smsEntity.setBatchId(batchId);
        smsEntity.setMessageId(messageId);
        smsEntity.setPartyId(partyId);
        smsEntity.setSender("Sundsvall");
        smsEntity.setMessage("message");
        smsEntity.setMobileNumber("+46701234567");
        smsEntity.setStatus(MessageStatus.PENDING);
        smsEntity.setCreatedAt(dateTimeNow);

        assertThat(smsEntity.getBatchId()).isEqualTo(batchId);
        assertThat(smsEntity.getMessageId()).isEqualTo(messageId);
        assertThat(smsEntity.getPartyId()).isEqualTo(partyId);
        assertThat(smsEntity.getSender()).isEqualTo("Sundsvall");
        assertThat(smsEntity.getMessage()).isEqualTo("message");
        assertThat(smsEntity.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(smsEntity.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(smsEntity.getCreatedAt()).isEqualTo(dateTimeNow);
    }
}

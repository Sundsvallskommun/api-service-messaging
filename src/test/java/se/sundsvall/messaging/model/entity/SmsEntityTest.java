package se.sundsvall.messaging.model.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;

class SmsEntityTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        SmsEntity sms = SmsEntity.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withSender("Sundsvall")
                .withMessage("message")
                .withMobileNumber("+46701234567")
                .withStatus(MessageStatus.PENDING)
                .withCreatedAt(dateTimeNow)
                .build();

        assertThat(sms.getBatchId()).isEqualTo(batchId);
        assertThat(sms.getMessageId()).isEqualTo(messageId);
        assertThat(sms.getPartyId()).isEqualTo(partyId);
        assertThat(sms.getSender()).isEqualTo("Sundsvall");
        assertThat(sms.getMessage()).isEqualTo("message");
        assertThat(sms.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(sms.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(sms.getCreatedAt()).isEqualTo(dateTimeNow);
    }

    @Test
    void testSetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        SmsEntity sms = new SmsEntity();
        sms.setBatchId(batchId);
        sms.setMessageId(messageId);
        sms.setPartyId(partyId);
        sms.setSender("Sundsvall");
        sms.setMessage("message");
        sms.setMobileNumber("+46701234567");
        sms.setStatus(MessageStatus.PENDING);
        sms.setCreatedAt(dateTimeNow);

        assertThat(sms.getBatchId()).isEqualTo(batchId);
        assertThat(sms.getMessageId()).isEqualTo(messageId);
        assertThat(sms.getPartyId()).isEqualTo(partyId);
        assertThat(sms.getSender()).isEqualTo("Sundsvall");
        assertThat(sms.getMessage()).isEqualTo("message");
        assertThat(sms.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(sms.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(sms.getCreatedAt()).isEqualTo(dateTimeNow);
    }

}

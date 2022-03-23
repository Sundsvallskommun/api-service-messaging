package se.sundsvall.messaging.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;

class SmsDtoTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();

        SmsDto sms = SmsDto.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withSender("Sundsvall")
                .withMessage("message")
                .withMobileNumber("+46701234567")
                .withStatus(MessageStatus.PENDING)
                .build();

        assertThat(sms.getBatchId()).isEqualTo(batchId);
        assertThat(sms.getMessageId()).isEqualTo(messageId);
        assertThat(sms.getPartyId()).isEqualTo(partyId);
        assertThat(sms.getSender()).isEqualTo("Sundsvall");
        assertThat(sms.getMessage()).isEqualTo("message");
        assertThat(sms.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(sms.getStatus()).isEqualTo(MessageStatus.PENDING);
    }

}

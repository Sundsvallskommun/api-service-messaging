package se.sundsvall.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

class SmsDtoTests {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();

        var smsDto = SmsDto.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withSender("Sundsvall")
            .withMessage("message")
            .withMobileNumber("+46701234567")
            .withStatus(MessageStatus.PENDING)
            .build();

        assertThat(smsDto.getBatchId()).isEqualTo(batchId);
        assertThat(smsDto.getMessageId()).isEqualTo(messageId);
        assertThat(smsDto.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(smsDto.getParty().getExternalReferences()).hasSize(1);
        assertThat(smsDto.getSender()).isEqualTo("Sundsvall");
        assertThat(smsDto.getMessage()).isEqualTo("message");
        assertThat(smsDto.getMobileNumber()).isEqualTo("+46701234567");
        assertThat(smsDto.getStatus()).isEqualTo(MessageStatus.PENDING);
    }

}

package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

class HistoryResponseTest {

    @Test
    void testBuilderAndGetters() {
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var response = HistoryResponse.builder()
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withMessage("message")
            .withSender("Sundsvall")
            .withTimestamp(now)
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.SENT)
            .build();

        assertThat(response.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(response.getParty().getExternalReferences()).hasSize(1);
        assertThat(response.getMessage()).isEqualTo("message");
        assertThat(response.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(response.getSender()).isEqualTo("Sundsvall");
        assertThat(response.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(response.getTimestamp()).isEqualTo(now);
    }

}

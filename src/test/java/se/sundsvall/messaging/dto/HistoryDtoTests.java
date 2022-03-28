package se.sundsvall.messaging.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

class HistoryDtoTests {

    @Test
    void testBuilderAndGetters() {
        var id = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var partyId = UUID.randomUUID().toString();
        var now = LocalDateTime.now();

        var historyDto = HistoryDto.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withParty(Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(ExternalReference.builder().build()))
                .build())
            .withCreatedAt(now)
            .withMessage("message")
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.PENDING)
            .withPartyContact("john.doe@example.com")
            .withSender("Sundsvalls kommun")
            .build();

        assertThat(historyDto.getBatchId()).isEqualTo(batchId);
        assertThat(historyDto.getMessageId()).isEqualTo(messageId);
        assertThat(historyDto.getParty().getPartyId()).isEqualTo(partyId);
        assertThat(historyDto.getParty().getExternalReferences()).hasSize(1);
        assertThat(historyDto.getCreatedAt()).isEqualTo(now);
        assertThat(historyDto.getMessage()).isEqualTo("message");
        assertThat(historyDto.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(historyDto.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(historyDto.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(historyDto.getSender()).isEqualTo("Sundsvalls kommun");
    }
}

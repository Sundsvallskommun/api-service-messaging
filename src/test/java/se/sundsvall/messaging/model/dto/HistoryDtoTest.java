package se.sundsvall.messaging.model.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

class HistoryDtoTest {

    @Test
    void testBuilderAndGetters() {
        String id = UUID.randomUUID().toString();
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        HistoryDto history = HistoryDto.builder()
                .withId(id)
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withPartyId(partyId)
                .withCreatedAt(dateTimeNow)
                .withMessage("message")
                .withMessageType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withPartyContact("john.doe@example.com")
                .withSender("Sundsvalls kommun")
                .build();

        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getBatchId()).isEqualTo(batchId);
        assertThat(history.getMessageId()).isEqualTo(messageId);
        assertThat(history.getPartyId()).isEqualTo(partyId);
        assertThat(history.getCreatedAt()).isEqualTo(dateTimeNow);
        assertThat(history.getMessage()).isEqualTo("message");
        assertThat(history.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(history.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(history.getPartyContact()).isEqualTo("john.doe@example.com");
        assertThat(history.getSender()).isEqualTo("Sundsvalls kommun");

        HistoryDto historyDto = history.toBuilder()
                .withBatchId("1")
                .build();

        assertThat(historyDto.getBatchId()).isEqualTo("1");
    }

}

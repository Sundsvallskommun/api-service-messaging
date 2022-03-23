package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;

class HistoryResponseTest {

    @Test
    void testBuilderAndGetters() {
        String partyId = UUID.randomUUID().toString();
        LocalDateTime dateTimeNow = LocalDateTime.now();

        HistoryResponse history = HistoryResponse.builder()
                .withPartyId(partyId)
                .withMessage("message")
                .withSender("Sundsvall")
                .withTimestamp(dateTimeNow)
                .withMessageType(MessageType.EMAIL)
                .withStatus(MessageStatus.SENT)
                .build();

        assertThat(history.getPartyId()).isEqualTo(partyId);
        assertThat(history.getMessage()).isEqualTo("message");
        assertThat(history.getMessageType()).isEqualTo(MessageType.EMAIL);
        assertThat(history.getSender()).isEqualTo("Sundsvall");
        assertThat(history.getStatus()).isEqualTo(MessageStatus.SENT);
        assertThat(history.getTimestamp()).isEqualTo(dateTimeNow);
    }

}

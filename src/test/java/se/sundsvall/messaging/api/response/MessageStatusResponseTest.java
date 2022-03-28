package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;

class MessageStatusResponseTest {

    @Test
    void testBuilderAndGetters() {
        var messageId = UUID.randomUUID().toString();

        var statusResponse = MessageStatusResponse.builder()
            .withMessageId(messageId)
            .withStatus(MessageStatus.SENT)
            .build();

        assertThat(statusResponse.getMessageId()).isEqualTo(messageId);
        assertThat(statusResponse.getStatus()).isEqualTo(MessageStatus.SENT);
    }
}

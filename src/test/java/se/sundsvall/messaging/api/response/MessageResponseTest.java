package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class MessageResponseTest {

    @Test
    void testGetter() {
        var messageId = UUID.randomUUID().toString();
        var messageResponse = new MessageResponse(messageId);

        assertThat(messageResponse.getMessageId()).isEqualTo(messageId);
    }
}

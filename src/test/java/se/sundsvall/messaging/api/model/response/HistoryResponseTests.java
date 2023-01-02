package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class HistoryResponseTests {

    @Test
    void testBuilderAndGetters() {
        var historyResponse = HistoryResponse.builder()
            .withMessageType(MessageType.WEB_MESSAGE)
            .withStatus(MessageStatus.SENT)
            .withContent("someContent")
            .withTimestamp(LocalDateTime.now())
            .build();

        assertThat(historyResponse.messageType()).isEqualTo(MessageType.WEB_MESSAGE);
        assertThat(historyResponse.status()).isEqualTo(MessageStatus.SENT);
        assertThat(historyResponse.content()).isEqualTo("someContent");
        assertThat(historyResponse.timestamp()).isNotNull();
    }
}

package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class HistoryTests {

    @Test
    void testDefaultConstructor() {
        var history = new History("someBatchId", "someMessageId", "someDeliveryId",
            MessageType.SNAIL_MAIL, MessageStatus.FAILED, "someContent", LocalDateTime.now());

        assertThat(history.batchId()).isEqualTo("someBatchId");
        assertThat(history.messageId()).isEqualTo("someMessageId");
        assertThat(history.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(history.messageType()).isEqualTo(MessageType.SNAIL_MAIL);
        assertThat(history.status()).isEqualTo(MessageStatus.FAILED);
        assertThat(history.content()).isEqualTo("someContent");
        assertThat(history.createdAt()).isNotNull();
    }
}

package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class HistoryTest {

    @Test
    void testDefaultConstructor() {
        var history = new History("someBatchId", "someMessageId", "someDeliveryId",
            SNAIL_MAIL, DIGITAL_MAIL, FAILED, "someContent", LocalDateTime.now());

        assertThat(history.batchId()).isEqualTo("someBatchId");
        assertThat(history.messageId()).isEqualTo("someMessageId");
        assertThat(history.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(history.messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(history.originalMessageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(history.status()).isEqualTo(FAILED);
        assertThat(history.content()).isEqualTo("someContent");
        assertThat(history.createdAt()).isNotNull();
    }
}

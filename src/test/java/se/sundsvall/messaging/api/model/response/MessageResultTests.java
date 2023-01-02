package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageResultTests {

    @Test
    void testBuilderAndGetters() {
        var messageResult = MessageResult.builder()
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withStatus(MessageStatus.AWAITING_FEEDBACK)
            .build();

        assertThat(messageResult.messageId()).isEqualTo("someMessageId");
        assertThat(messageResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(messageResult.status()).isEqualTo(MessageStatus.AWAITING_FEEDBACK);
    }
}

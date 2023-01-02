package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class DeliveryResultTests {

    @Test
    void testBuilderAndGetters() {
        var deliveryResult = DeliveryResult.builder()
            .withDeliveryId("someDeliveryId")
            .withMessageType(SMS)
            .withStatus(SENT)
            .build();

        assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
        assertThat(deliveryResult.messageType()).isEqualTo(SMS);
        assertThat(deliveryResult.status()).isEqualTo(SENT);
    }
}

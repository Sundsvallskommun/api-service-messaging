package se.sundsvall.messaging.service.event;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageType.SMS;

@UnitTest
class IncomingMessageEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingMessageEvent("someSource", SMS, "someDeliveryId", "someOrigin");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getMessageType()).isEqualTo(SMS);
        assertThat(event.getDeliveryId()).isEqualTo("someDeliveryId");
        assertThat(event.getOrigin()).isEqualTo("someOrigin");
    }
}

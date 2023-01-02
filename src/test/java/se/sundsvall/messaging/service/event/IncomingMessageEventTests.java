package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class IncomingMessageEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingMessageEvent("someSource", MessageType.SMS, "someDeliveryId");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getMessageType()).isEqualTo(MessageType.SMS);
        assertThat(event.getDeliveryId()).isEqualTo("someDeliveryId");
    }
}

package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IncomingDigitalMailEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingDigitalMailEvent("someSource", "someMessageId");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getMessageId()).isEqualTo("someMessageId");
    }
}

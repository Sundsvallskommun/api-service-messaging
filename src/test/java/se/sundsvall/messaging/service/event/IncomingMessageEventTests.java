package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IncomingMessageEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingMessageEvent("someSource", 12345L);

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getPayload()).isEqualTo(12345L);
    }
}

package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IncomingEmailEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingEmailEvent("someSource", "someMessageId");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getPayload()).isEqualTo("someMessageId");
    }
}

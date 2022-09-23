package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class IncomingWebMessageEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingWebMessageEvent("someSource", "someMessageId");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getPayload()).isEqualTo("someMessageId");
    }
}

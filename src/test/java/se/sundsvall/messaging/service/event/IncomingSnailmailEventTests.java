package se.sundsvall.messaging.service.event;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IncomingSnailmailEventTests {

    @Test
    void testConstructorAndGetter() {
        var event = new IncomingSnailmailEvent("someSource", "someMessageId");

        assertThat(event.getSource()).isEqualTo("someSource");
        assertThat(event.getPayload()).isEqualTo("someMessageId");
    }
}

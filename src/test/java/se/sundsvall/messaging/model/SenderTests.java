package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createSender;

import org.junit.jupiter.api.Test;

class SenderTests {

    @Test
    void testBuilderAndGetters() {
        var sender = createSender(s -> {
            s.getSms().setName("someName");
            s.getEmail().setName("someName");
            s.getEmail().setAddress("someAddress");
            s.getEmail().setReplyTo("someReplyTo");
        });

        assertThat(sender.getSms()).isNotNull().satisfies(sms ->
            assertThat(sms.getName()).isEqualTo("someName")
        );
        assertThat(sender.getEmail()).isNotNull().satisfies(email -> {
            assertThat(email.getName()).isEqualTo("someName");
            assertThat(email.getAddress()).isEqualTo("someAddress");
            assertThat(email.getReplyTo()).isEqualTo("someReplyTo");
        });
    }
}

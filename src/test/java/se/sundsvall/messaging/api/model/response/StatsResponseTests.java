package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.Statistics;
import se.sundsvall.messaging.model.Statistics.Count;
import se.sundsvall.messaging.model.Statistics.Letter;
import se.sundsvall.messaging.model.Statistics.Message;

class StatsResponseTests {

    @Test
    void testBuilderAndGetters() {
        var response = Statistics.builder()
            .withEmail(Count.builder().withSent(1).withFailed(2).build())
            .withSms(Count.builder().withSent(1).withFailed(0).build())
            .withDigitalMail(Count.builder().withSent(1).withFailed(1).build())
            .withWebMessage(Count.builder().withSent(2).withFailed(2).build())
            .withSnailMail(Count.builder().withSent(0).withFailed(1).build())
            .withMessage(Message.builder()
                .withEmail(Count.builder().withSent(1).withFailed(1).build())
                .withSms(Count.builder().withSent(2).withFailed(2).build())
                .withUndeliverable(5)
                .build())
            .withLetter(Letter.builder()
                .withDigitalMail(Count.builder().withSent(4).withFailed(0).build())
                .withSnailMail(Count.builder().withSent(1).withFailed(3).build())
                .build())
            .build();

        assertThat(response).isNotNull();
        assertThat(response.email()).isNotNull().satisfies(email -> {
            assertThat(email.sent()).isEqualTo(1);
            assertThat(email.failed()).isEqualTo(2);
            assertThat(email.total()).isEqualTo(3);
        });
        assertThat(response.sms()).isNotNull().satisfies(sms -> {
            assertThat(sms.sent()).isEqualTo(1);
            assertThat(sms.failed()).isEqualTo(0);
            assertThat(sms.total()).isEqualTo(1);
        });
        assertThat(response.digitalMail()).isNotNull().satisfies(digitalMail -> {
            assertThat(digitalMail.sent()).isEqualTo(1);
            assertThat(digitalMail.failed()).isEqualTo(1);
            assertThat(digitalMail.total()).isEqualTo(2);
        });
        assertThat(response.webMessage()).isNotNull().satisfies(webMessage -> {
            assertThat(webMessage.sent()).isEqualTo(2);
            assertThat(webMessage.failed()).isEqualTo(2);
            assertThat(webMessage.total()).isEqualTo(4);
        });
        assertThat(response.snailMail()).isNotNull().satisfies(snailMail -> {
            assertThat(snailMail.sent()).isEqualTo(0);
            assertThat(snailMail.failed()).isEqualTo(1);
            assertThat(snailMail.total()).isEqualTo(1);
        });
        assertThat(response.message()).isNotNull().satisfies(message -> {
            assertThat(message.sms()).isNotNull().satisfies(sms -> {
                assertThat(sms.sent()).isEqualTo(2);
                assertThat(sms.failed()).isEqualTo(2);
                assertThat(sms.total()).isEqualTo(4);
            });
            assertThat(message.email()).isNotNull().satisfies(email -> {
                assertThat(email.sent()).isEqualTo(1);
                assertThat(email.failed()).isEqualTo(1);
                assertThat(email.total()).isEqualTo(2);
            });
            assertThat(message.undeliverable()).isEqualTo(5);
            assertThat(message.total()).isEqualTo(11);
        });
        assertThat(response.letter()).isNotNull().satisfies(letter -> {
            assertThat(letter.digitalMail()).isNotNull().satisfies(digitalMail -> {
                assertThat(digitalMail.sent()).isEqualTo(4);
                assertThat(digitalMail.failed()).isEqualTo(0);
                assertThat(digitalMail.total()).isEqualTo(4);
            });
            assertThat(letter.snailMail()).isNotNull().satisfies(snailMail -> {
                assertThat(snailMail.sent()).isEqualTo(1);
                assertThat(snailMail.failed()).isEqualTo(3);
                assertThat(snailMail.total()).isEqualTo(4);
            });
            assertThat(letter.total()).isEqualTo(8);
        });
        assertThat(response.total()).isEqualTo(30);
    }
}

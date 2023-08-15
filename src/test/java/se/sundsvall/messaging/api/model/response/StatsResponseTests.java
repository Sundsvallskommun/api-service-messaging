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
            assertCount(email, 1, 2);
        });
        assertThat(response.sms()).isNotNull().satisfies(sms -> {
            assertCount(sms, 1, 0);
        });
        assertThat(response.digitalMail()).isNotNull().satisfies(digitalMail -> {
            assertCount(digitalMail, 1, 1);
        });
        assertThat(response.webMessage()).isNotNull().satisfies(webMessage -> {
            assertCount(webMessage, 2, 2);
        });
        assertThat(response.snailMail()).isNotNull().satisfies(snailMail -> {
            assertCount(snailMail, 0, 1);
        });
        assertThat(response.message()).isNotNull().satisfies(message -> {
            assertThat(message.sms()).isNotNull().satisfies(sms -> {
                assertCount(sms, 2, 2);
            });
            assertThat(message.email()).isNotNull().satisfies(email -> {
                assertCount(email, 1, 1);
            });
            assertThat(message.undeliverable()).isEqualTo(5);
            assertThat(message.total()).isEqualTo(11);
        });
        assertThat(response.letter()).isNotNull().satisfies(letter -> {
            assertThat(letter.digitalMail()).isNotNull().satisfies(digitalMail -> {
                assertCount(digitalMail, 4, 0);
            });
            assertThat(letter.snailMail()).isNotNull().satisfies(snailMail -> {
                assertCount(snailMail, 1, 3);
            });
            assertThat(letter.total()).isEqualTo(8);
        });
        assertThat(response.total()).isEqualTo(30);
    }

    private void assertCount(final Statistics.Count count, final int expectedSent, final int expectedFailed) {
        assertThat(count.sent()).isEqualTo(expectedSent);
        assertThat(count.failed()).isEqualTo(expectedFailed);
    }
}

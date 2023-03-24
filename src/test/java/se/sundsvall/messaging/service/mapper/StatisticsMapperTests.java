package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_FEEDBACK_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.integration.db.projection.StatsEntry;

class StatisticsMapperTests {

    private final StatisticsMapper mapper = new StatisticsMapper();

    @Test
    void test_toStatistics() {
        var input = List.of(
            // MESSAGE
            new StatsEntry(SMS, MESSAGE, SENT),
            new StatsEntry(SMS, MESSAGE, SENT),
            new StatsEntry(SMS, MESSAGE, FAILED),
            new StatsEntry(EMAIL, MESSAGE, SENT),
            new StatsEntry(MESSAGE, MESSAGE, NO_FEEDBACK_WANTED),
            // LETTER
            new StatsEntry(DIGITAL_MAIL, LETTER, SENT),
            new StatsEntry(DIGITAL_MAIL, LETTER, FAILED),
            new StatsEntry(SNAIL_MAIL, LETTER, FAILED),
            // EMAIL
            new StatsEntry(EMAIL, EMAIL, SENT),
            new StatsEntry(EMAIL, EMAIL, SENT),
            new StatsEntry(EMAIL, EMAIL, FAILED),
            // SMS
            new StatsEntry(SMS, SMS, SENT),
            new StatsEntry(SMS, SMS, FAILED),
            new StatsEntry(SMS, SMS, FAILED),
            // WEB_MESSAGE
            new StatsEntry(WEB_MESSAGE, WEB_MESSAGE, SENT),
            new StatsEntry(WEB_MESSAGE, WEB_MESSAGE, FAILED),
            // DIGITAL_MAIL
            new StatsEntry(DIGITAL_MAIL, DIGITAL_MAIL, SENT),
            // SNAIL_MAIL
            new StatsEntry(SNAIL_MAIL, SNAIL_MAIL, FAILED)
        );

        var result = mapper.toStatistics(input);

        assertThat(result).isNotNull();
        assertThat(result.email()).satisfies(email -> {
            assertThat(email.sent()).isEqualTo(2);
            assertThat(email.failed()).isOne();
            assertThat(email.total()).isEqualTo(3);
        });
        assertThat(result.sms()).satisfies(sms -> {
            assertThat(sms.sent()).isOne();
            assertThat(sms.failed()).isEqualTo(2);
            assertThat(sms.total()).isEqualTo(3);
        });
        assertThat(result.digitalMail()).satisfies(digitalMail -> {
            assertThat(digitalMail.sent()).isOne();
            assertThat(digitalMail.failed()).isZero();
            assertThat(digitalMail.total()).isOne();
        });
        assertThat(result.webMessage()).satisfies(webMessage -> {
            assertThat(webMessage.sent()).isOne();
            assertThat(webMessage.failed()).isOne();
            assertThat(webMessage.total()).isEqualTo(2);
        });
        assertThat(result.snailMail()).satisfies(snailMail -> {
            assertThat(snailMail.sent()).isZero();
            assertThat(snailMail.failed()).isOne();
            assertThat(snailMail.total()).isOne();
        });
        assertThat(result.message()).isNotNull().satisfies(message -> {
            assertThat(message.email()).isNotNull().satisfies(email -> {
                assertThat(email.sent()).isOne();
                assertThat(email.failed()).isZero();
                assertThat(email.total()).isOne();
            });
            assertThat(message.sms()).isNotNull().satisfies(sms -> {
                assertThat(sms.sent()).isEqualTo(2);
                assertThat(sms.failed()).isOne();
                assertThat(sms.total()).isEqualTo(3);
            });
            assertThat(message.undeliverable()).isEqualTo(1);
        });
        assertThat(result.letter()).isNotNull().satisfies(letter -> {
            assertThat(letter.digitalMail()).isNotNull().satisfies(digitalMail -> {
                assertThat(digitalMail.sent()).isOne();
                assertThat(digitalMail.failed()).isOne();
                assertThat(digitalMail.total()).isEqualTo(2);
            });
            assertThat(letter.snailMail()).isNotNull().satisfies(snailMail -> {
                assertThat(snailMail.sent()).isZero();
                assertThat(snailMail.failed()).isOne();
                assertThat(snailMail.total()).isOne();
            });
        });
        assertThat(result.total()).isEqualTo(18);
    }

    @Test
    void test_toCount() {
        var input = Map.of(SENT, 3, FAILED, 5);

        var result = mapper.toCount(input);

        assertThat(result.sent()).isEqualTo(3);
        assertThat(result.failed()).isEqualTo(5);
        assertThat(result.total()).isEqualTo(8);
    }

    @Test
    void test_toCount_withNoSENT() {
        var input = Map.of(FAILED, 4);

        var result = mapper.toCount(input);

        assertThat(result.sent()).isZero();
        assertThat(result.failed()).isEqualTo(4);
        assertThat(result.total()).isEqualTo(4);
    }

    @Test
    void test_toCount_withNoFAILED() {
        var input = Map.of(SENT, 7);

        var result = mapper.toCount(input);

        assertThat(result.sent()).isEqualTo(7);
        assertThat(result.failed()).isZero();
        assertThat(result.total()).isEqualTo(7);
    }
}

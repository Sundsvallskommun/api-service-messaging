package se.sundsvall.messaging.service.mapper;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toCount;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStatisticsList;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toStatistics;

class StatisticsMapperTest {

    @Test
    void test_toStatistics() {
        var input = List.of(
            // MESSAGE
            new StatsEntry(SMS, MESSAGE, SENT),
            new StatsEntry(SMS, MESSAGE, SENT),
            new StatsEntry(SMS, MESSAGE, FAILED),
            new StatsEntry(EMAIL, MESSAGE, SENT),
            new StatsEntry(MESSAGE, MESSAGE, NO_CONTACT_WANTED),
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

        var result = toStatistics(input);

        assertThat(result).isNotNull();
        assertThat(result.email()).satisfies(email -> assertCount(email, 2, 1));
        assertThat(result.sms()).satisfies(sms -> assertCount(sms, 1, 2));
        assertThat(result.digitalMail()).satisfies(digitalMail -> assertCount(digitalMail, 1, 0));
        assertThat(result.webMessage()).satisfies(webMessage -> assertCount(webMessage, 1, 1));
        assertThat(result.snailMail()).satisfies(snailMail -> assertCount(snailMail, 0, 1));
        assertThat(result.message()).isNotNull().satisfies(message -> {
            assertThat(message.email()).isNotNull().satisfies(email -> assertCount(email, 1, 0));
            assertThat(message.sms()).isNotNull().satisfies(sms -> assertCount(sms, 2, 1));
            assertThat(message.undeliverable()).isOne();
        });
        assertThat(result.letter()).isNotNull().satisfies(letter -> {
            assertThat(letter.digitalMail()).isNotNull().satisfies(digitalMail -> assertCount(digitalMail, 1, 1));
            assertThat(letter.snailMail()).isNotNull().satisfies(snailMail -> assertCount(snailMail, 0, 1));
        });
        assertThat(result.total()).isEqualTo(18);
    }

    @Test
    void test_toDepartmentStatistics() {
        final var department_1 = "department_1";
        final var department_2 = "department_2";
        final var origin_1 = "origin_1";
        final var origin_2 = "origin_2";

        var input = List.of(
            // ORIGIN_1
            new StatsEntry(DIGITAL_MAIL, LETTER, SENT, origin_1, department_1),
            new StatsEntry(DIGITAL_MAIL, LETTER, FAILED, origin_1, department_1),
            new StatsEntry(DIGITAL_MAIL, LETTER, FAILED, origin_1, department_2),
            // Department 2
            new StatsEntry(SNAIL_MAIL, LETTER, SENT, origin_2, department_2),
            new StatsEntry(SNAIL_MAIL, LETTER, FAILED, origin_2, department_2),
            new StatsEntry(SNAIL_MAIL, LETTER, FAILED, origin_2, department_1),
            // Other
            new StatsEntry(SNAIL_MAIL, LETTER, SENT, origin_1, null),
            new StatsEntry(SNAIL_MAIL, LETTER, FAILED, origin_2, null),
            new StatsEntry(DIGITAL_MAIL, LETTER, FAILED)); // No department and no origin

        var result = toDepartmentStatisticsList(input);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3)
            .extracting(DepartmentStatistics::origin, DepartmentStatistics::departmentLetters).containsExactly(
                tuple(origin_1, List.of(DepartmentLetter.builder()
                        .withDepartment(department_1)
                        .withDigitalMail(Count.builder().withSent(1).withFailed(1).build())
                        .build(),
                    DepartmentLetter.builder()
                        .withDepartment(department_2)
                        .withDigitalMail(Count.builder().withSent(0).withFailed(1).build())
                        .build(),
                    DepartmentLetter.builder()
                        .withDepartment("Other")
                        .withSnailMail(Count.builder().withSent(1).withFailed(0).build())
                        .build())),
                tuple(origin_2, List.of(DepartmentLetter.builder()
                        .withDepartment(department_1)
                        .withSnailMail(Count.builder().withSent(0).withFailed(1).build())
                        .build(),
                    DepartmentLetter.builder()
                        .withDepartment(department_2)
                        .withSnailMail(Count.builder().withSent(1).withFailed(1).build())
                        .build(),
                    DepartmentLetter.builder()
                        .withDepartment("Other")
                        .withSnailMail(Count.builder().withSent(0).withFailed(1).build())
                        .build())),
                tuple("Other", List.of(DepartmentLetter.builder()
                        .withDepartment("Other")
                        .withDigitalMail(Count.builder().withSent(0).withFailed(1).build())
                        .build())));
    }

    private void assertCount(final Count count, final int expectedSent, final int expectedFailed) {
        assertThat(count.sent()).isEqualTo(expectedSent);
        assertThat(count.failed()).isEqualTo(expectedFailed);
    }

    @Test
    void test_toCount() {
        var input = Map.of(SENT, 3, FAILED, 5);

        var result = toCount(input);

        assertThat(result.sent()).isEqualTo(3);
        assertThat(result.failed()).isEqualTo(5);
        assertThat(result.total()).isEqualTo(8);
    }

    @Test
    void test_toCount_withNoSENT() {
        var input = Map.of(FAILED, 4);

        var result = toCount(input);

        assertThat(result.sent()).isZero();
        assertThat(result.failed()).isEqualTo(4);
        assertThat(result.total()).isEqualTo(4);
    }

    @Test
    void test_toCount_withNoFAILED() {
        var input = Map.of(SENT, 7);

        var result = toCount(input);

        assertThat(result.sent()).isEqualTo(7);
        assertThat(result.failed()).isZero();
        assertThat(result.total()).isEqualTo(7);
    }
}

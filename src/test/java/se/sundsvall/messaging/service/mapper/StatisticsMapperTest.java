package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.createStatisticsEntity;
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
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.mapToCount;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toCount;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStatisticsList;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toDepartmentStats;
import static se.sundsvall.messaging.service.mapper.StatisticsMapper.toStatistics;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.sundsvall.messaging.integration.db.entity.StatisticEntity;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class StatisticsMapperTest {

	@Test
	void test_toDepartmentStats() {
		final var department = "department";
		final var origin = "origin";
		final var statsProjections = List.of(
			createStatisticsEntity(SMS, SMS, SENT, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(SMS, SMS, FAILED, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, null, null, MUNICIPALITY_ID));

		final var result = toDepartmentStats(statsProjections, department, origin);

		assertThat(result).isNotNull().satisfies(departmentStats -> {
			assertThat(departmentStats.department()).isEqualTo(department);
			assertThat(departmentStats.origin()).isEqualTo(origin);
			assertThat(departmentStats.sms()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.digitalMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.snailMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
		});
	}

	@Test
	void test_mapToCount() {
		final var statsProjections = List.of(
			createStatisticsEntity(SMS, SMS, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SMS, SMS, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID));

		final var smsResult = mapToCount(statsProjections, SMS);
		final var snailMailResult = mapToCount(statsProjections, SNAIL_MAIL);
		final var digitalMailResult = mapToCount(statsProjections, DIGITAL_MAIL);

		assertThat(smsResult).isNotNull().satisfies(count -> {
			assertThat(count.sent()).isEqualTo(1);
			assertThat(count.failed()).isEqualTo(1);
			assertThat(count.total()).isEqualTo(2);
		});

		assertThat(snailMailResult).isNotNull().satisfies(count -> {
			assertThat(count.sent()).isEqualTo(1);
			assertThat(count.failed()).isEqualTo(1);
			assertThat(count.total()).isEqualTo(2);
		});

		assertThat(digitalMailResult).isNotNull().satisfies(count -> {
			assertThat(count.sent()).isEqualTo(1);
			assertThat(count.failed()).isEqualTo(1);
			assertThat(count.total()).isEqualTo(2);
		});
	}

	@Test
	void test_toStatistics() {
		final var input = List.of(
			createStatisticsEntity(DIGITAL_MAIL, DIGITAL_MAIL, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, DIGITAL_MAIL, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(SNAIL_MAIL, SNAIL_MAIL, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, SNAIL_MAIL, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(SMS, SMS, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SMS, SMS, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(EMAIL, EMAIL, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(EMAIL, EMAIL, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(WEB_MESSAGE, WEB_MESSAGE, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(WEB_MESSAGE, WEB_MESSAGE, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(EMAIL, MESSAGE, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(EMAIL, MESSAGE, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SMS, MESSAGE, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SMS, MESSAGE, FAILED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(MESSAGE, MESSAGE, NO_CONTACT_WANTED, "origin", "department", MUNICIPALITY_ID),

			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID));

		final var result = toStatistics(input);

		assertThat(result).isNotNull();
		assertThat(result.email()).satisfies(email -> assertCount(email, 1, 1));
		assertThat(result.sms()).satisfies(sms -> assertCount(sms, 1, 1));
		assertThat(result.digitalMail()).satisfies(digitalMail -> assertCount(digitalMail, 1, 1));
		assertThat(result.webMessage()).satisfies(webMessage -> assertCount(webMessage, 1, 1));
		assertThat(result.snailMail()).satisfies(snailMail -> assertCount(snailMail, 1, 1));
		assertThat(result.message()).isNotNull().satisfies(message -> {
			assertThat(message.email()).isNotNull().satisfies(email -> assertCount(email, 1, 1));
			assertThat(message.sms()).isNotNull().satisfies(sms -> assertCount(sms, 1, 1));
			assertThat(message.undeliverable()).isOne();
		});
		assertThat(result.letter()).isNotNull().satisfies(letter -> {
			assertThat(letter.digitalMail()).isNotNull().satisfies(digitalMail -> assertCount(digitalMail, 1, 1));
			assertThat(letter.snailMail()).isNotNull().satisfies(snailMail -> assertCount(snailMail, 1, 1));
		});
		assertThat(result.total()).isEqualTo(19);
	}

	@Test
	void test_toDepartmentStatistics() {
		final var input = List.of(
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, "origin", "department", MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, null, null, MUNICIPALITY_ID),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, null, null, MUNICIPALITY_ID));

		final var result = toDepartmentStatisticsList(input, MUNICIPALITY_ID);

		assertThat(result).isNotNull().hasSize(2)
			.extracting(DepartmentStatistics::origin, DepartmentStatistics::departmentLetters).containsExactly(
				tuple("origin", List.of(
					DepartmentLetter.builder()
						.withDepartment("department")
						.withSnailMail(Count.builder().withSent(1).withFailed(1).build())
						.withDigitalMail(Count.builder().withSent(1).withFailed(1).build())
						.build())),
				tuple("Other", List.of(
					DepartmentLetter.builder()
						.withDepartment("Ej kategoriserad")
						.withSnailMail(Count.builder().withSent(1).withFailed(1).build())
						.withDigitalMail(Count.builder().withSent(1).withFailed(1).build())
						.build())));
	}

	private void assertCount(final Count count, final int expectedSent, final int expectedFailed) {
		assertThat(count.sent()).isEqualTo(expectedSent);
		assertThat(count.failed()).isEqualTo(expectedFailed);
	}

	@Test
	void test_toCount() {
		final var input = Map.of(SENT, 3, FAILED, 5);

		final var result = toCount(input);

		assertThat(result.sent()).isEqualTo(3);
		assertThat(result.failed()).isEqualTo(5);
		assertThat(result.total()).isEqualTo(8);
	}

	@Test
	void test_toCount_withNoSENT() {
		final var input = Map.of(FAILED, 4);

		final var result = toCount(input);

		assertThat(result.sent()).isZero();
		assertThat(result.failed()).isEqualTo(4);
		assertThat(result.total()).isEqualTo(4);
	}

	@Test
	void test_toCount_withNoFAILED() {
		final var input = Map.of(SENT, 7);

		final var result = toCount(input);

		assertThat(result.sent()).isEqualTo(7);
		assertThat(result.failed()).isZero();
		assertThat(result.total()).isEqualTo(7);
	}

	@Test
	void overrideEntryValues() {
		final var projectionMock = Mockito.mock(StatisticEntity.class);
		when(projectionMock.getStatus()).thenReturn(MessageStatus.SENT);
		when(projectionMock.getOriginalMessageType()).thenReturn(MessageType.SMS);
		when(projectionMock.getMessageType()).thenReturn(MessageType.SNAIL_MAIL);

		final var result = StatisticsMapper.overrideEntryValues(projectionMock, "origin", "department", "municipalityId");

		assertThat(result.getStatus()).isEqualTo(MessageStatus.SENT);
		assertThat(result.getOriginalMessageType()).isEqualTo(MessageType.SMS);
		assertThat(result.getMessageType()).isEqualTo(MessageType.SNAIL_MAIL);
		assertThat(result.getOrigin()).isEqualTo("origin");
		assertThat(result.getDepartment()).isEqualTo("department");
		assertThat(result.getMunicipalityId()).isEqualTo("municipalityId");

	}
}

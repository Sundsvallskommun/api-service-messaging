package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createStatisticsEntity;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

	@Mock
	private DbIntegration mockDbIntegration;

	@InjectMocks
	private StatisticsService statisticsService;

	@Test
	void getStatistics() {
		final var messageType = SMS;
		final var now = LocalDate.now();
		final var municipalityId = "2281";
		final var statsProjection = createStatisticsEntity(SMS, SMS, SENT, null, null, municipalityId);

		when(mockDbIntegration.getStatsByParameters(municipalityId, null, null, List.of(messageType), now, now.plusMonths(1)))
			.thenReturn(List.of(statsProjection));

		final var result = statisticsService.getStatistics(messageType, now, now.plusMonths(1), municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.sms()).extracting(Count::sent).isEqualTo(1);

		verify(mockDbIntegration).getStatsByParameters(municipalityId, null, null, List.of(messageType), now, now.plusMonths(1));
	}

	@Test
	void getDepartmentLetterStats() {
		final var department = "department";
		final var origin = "origin";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusMonths(1);
		final var municipalityId = "2281";
		final var statsProjection = createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, origin, department, municipalityId);

		when(mockDbIntegration.getStatsByParameters(anyString(), anyString(), anyString(), anyList(), any(LocalDate.class), any(LocalDate.class)))
			.thenReturn(List.of(statsProjection));

		final var result = statisticsService.getDepartmentLetterStatistics(origin, department, fromDate, toDate, municipalityId);

		assertThat(result).hasSize(1)
			.extracting(DepartmentStatistics::origin, DepartmentStatistics::departmentLetters).containsExactly(
				tuple(origin, List.of(DepartmentLetter.builder()
					.withDepartment(department)
					.withSnailMail(Count.builder().withSent(1).withFailed(0).build())
					.build())));

		verify(mockDbIntegration).getStatsByParameters(municipalityId, origin, department, List.of(LETTER), fromDate, toDate);
	}

	@Test
	void getStatisticsByDepartment() {
		final var municipalityId = "2281";
		final var department = "department";
		final var origin = "origin";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusMonths(1);
		final var messageTypes = List.of(LETTER, SMS);
		final var statProjections = List.of(
			createStatisticsEntity(SMS, SMS, SENT, origin, department, municipalityId),
			createStatisticsEntity(SMS, SMS, FAILED, origin, department, municipalityId),
			createStatisticsEntity(SNAIL_MAIL, LETTER, SENT, origin, department, municipalityId),
			createStatisticsEntity(SNAIL_MAIL, LETTER, FAILED, origin, department, municipalityId),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, SENT, origin, department, municipalityId),
			createStatisticsEntity(DIGITAL_MAIL, LETTER, FAILED, origin, department, municipalityId));

		when(mockDbIntegration.getStatsByParameters(municipalityId, origin, department, messageTypes, fromDate, toDate)).thenReturn(statProjections);

		final var result = statisticsService.getStatisticsByDepartment(municipalityId, department, origin, fromDate, toDate);

		assertThat(result).isNotNull().satisfies(departmentStats -> {
			assertThat(departmentStats.origin()).isEqualTo(origin);
			assertThat(departmentStats.department()).isEqualTo(department);
			assertThat(departmentStats.sms()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.snailMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.digitalMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
		});

		verify(mockDbIntegration).getStatsByParameters(municipalityId, origin, department, messageTypes, fromDate, toDate);
		verifyNoMoreInteractions(mockDbIntegration);
	}

}

package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.Count;
import se.sundsvall.messaging.model.DepartmentLetter;
import se.sundsvall.messaging.model.DepartmentStatistics;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

	@Mock
	private DbIntegration mockDbIntegration;

	@InjectMocks
	private StatisticsService statisticsService;

	@Test
	void getStats() {
		final var municipalityId = "2281";
		when(mockDbIntegration.getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class), any(String.class)))
			.thenReturn(List.of(new StatsEntry(SMS, SMS, SENT, municipalityId)));

		final var result = statisticsService.getStatistics(SMS, LocalDate.now(), LocalDate.now().plusMonths(1), municipalityId);

		assertThat(result).isNotNull();

		assertThat(result.sms()).extracting(Count::sent).isEqualTo(1);

		verify(mockDbIntegration).getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class), any(String.class));
	}

	@Test
	void getDepartmentLetterStats() {
		final var department = "department";
		final var origin = "origin";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusMonths(1);
		final var municipalityId = "2281";
		when(mockDbIntegration.getStatsByMunicipalityIdAndOriginAndDepartment(anyString(), anyString(), anyString(), any(MessageType.class), any(LocalDate.class), any(LocalDate.class)))
			.thenReturn(List.of(new StatsEntry(SNAIL_MAIL, LETTER, SENT, origin, department, municipalityId)));

		final var result = statisticsService.getDepartmentLetterStatistics(origin, department, fromDate, toDate, municipalityId);

		assertThat(result).hasSize(1)
			.extracting(DepartmentStatistics::origin, DepartmentStatistics::departmentLetters).containsExactly(
				tuple(origin, List.of(DepartmentLetter.builder()
					.withDepartment(department)
					.withSnailMail(Count.builder().withSent(1).withFailed(0).build())
					.build())));

		verify(mockDbIntegration).getStatsByMunicipalityIdAndOriginAndDepartment(municipalityId, origin, department, LETTER, fromDate, toDate);
	}

	@Test
	void getStatisticsByDepartment() {
		final var municipalityId = "2281";
		final var department = "department";
		final var origin = "origin";
		final var fromDate = LocalDate.now();
		final var toDate = LocalDate.now().plusMonths(1);
		final var messageTypes = List.of(LETTER, SMS);
		final var statEntries = List.of(
			new StatsEntry(SMS, SMS, SENT, origin, department, municipalityId),
			new StatsEntry(SMS, SMS, FAILED, origin, department, municipalityId),
			new StatsEntry(SNAIL_MAIL, LETTER, SENT, origin, department, municipalityId),
			new StatsEntry(SNAIL_MAIL, LETTER, FAILED, origin, department, municipalityId),
			new StatsEntry(DIGITAL_MAIL, LETTER, SENT, origin, department, municipalityId),
			new StatsEntry(DIGITAL_MAIL, LETTER, FAILED, origin, department, municipalityId));

		when(mockDbIntegration.getStatsByMunicipalityIdAndDepartmentAndOriginAndAndMessageTypes(municipalityId, department, origin, messageTypes, fromDate, toDate)).thenReturn(statEntries);

		final var result = statisticsService.getStatisticsByDepartment(municipalityId, department, origin, fromDate, toDate);

		assertThat(result).isNotNull().satisfies(departmentStats -> {
			assertThat(departmentStats.origin()).isEqualTo(origin);
			assertThat(departmentStats.department()).isEqualTo(department);
			assertThat(departmentStats.sms()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.snailMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
			assertThat(departmentStats.digitalMail()).usingRecursiveComparison().isEqualTo(new Count(1, 1));
		});

		verify(mockDbIntegration).getStatsByMunicipalityIdAndDepartmentAndOriginAndAndMessageTypes(municipalityId, department, origin, messageTypes, fromDate, toDate);
		verifyNoMoreInteractions(mockDbIntegration);
	}

}

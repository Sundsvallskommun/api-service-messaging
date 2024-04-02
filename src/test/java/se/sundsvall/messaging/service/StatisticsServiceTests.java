package se.sundsvall.messaging.service;

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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTests {

    @Mock
    private DbIntegration mockDbIntegration;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void getStats() {
        when(mockDbIntegration.getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(new StatsEntry(SMS, SMS, SENT)));

        var result = statisticsService.getStatistics(SMS, LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThat(result).isNotNull();

        assertThat(result.sms()).extracting(Count::sent).isEqualTo(1);

        verify(mockDbIntegration).getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getDepartmentLetterStats() {
        final var department = "department";
        final var origin = "origin";
        final var fromDate = LocalDate.now();
        final var toDate = LocalDate.now().plusMonths(1);
        when(mockDbIntegration.getStatsByOriginAndDepartment(anyString(), anyString(), any(MessageType.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(new StatsEntry(SNAIL_MAIL, LETTER, SENT, origin, department)));

        var result = statisticsService.getDepartmentLetterStatistics(origin, department, fromDate, toDate);

        assertThat(result).hasSize(1)
            .extracting(DepartmentStatistics::origin, DepartmentStatistics::departmentLetters).containsExactly(
                tuple(origin, List.of(DepartmentLetter.builder()
                        .withDepartment(department)
                        .withSnailMail(Count.builder().withSent(1).withFailed(0).build())
                        .build())));

        verify(mockDbIntegration).getStatsByOriginAndDepartment(origin, department, LETTER, fromDate, toDate);
    }
}

package se.sundsvall.messaging.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.mapper.StatisticsMapper;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatisticsServiceTests {

    @Mock
    private DbIntegration mockDbIntegration;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private StatisticsMapper mockStatisticsMapper;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    void test_getStats() {
        when(mockDbIntegration.getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(new StatsEntry(SMS, SMS, SENT)));

        var result = statisticsService.getStatistics(SMS, LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThat(result).isNotNull();

        verify(mockDbIntegration).getStats(any(MessageType.class), any(LocalDate.class), any(LocalDate.class));
        verify(mockStatisticsMapper).toStatistics(anyList());
    }

    @Test
    void test_getDepartmentLetterStats() {
        final var department = "department";
        when(mockDbIntegration.getStatsByDepartment(anyString(), any(MessageType.class), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of(new StatsEntry(SMS, SMS, SENT, department)));

        var result = statisticsService.getDepartmentLetterStatistics(department, LocalDate.now(), LocalDate.now().plusMonths(1));

        assertThat(result).isNotNull();

        verify(mockDbIntegration).getStatsByDepartment(anyString(), any(MessageType.class), any(LocalDate.class), any(LocalDate.class));
        verify(mockStatisticsMapper).toDepartmentStatistics(anyList());
    }
}

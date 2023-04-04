package se.sundsvall.messaging.integration.db;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class StatisticsRepositoryTests {

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private StatisticsRepository mockStatisticsRepository;

    @Captor
    private ArgumentCaptor<LocalDateTime> fromCaptor;
    @Captor
    private ArgumentCaptor<LocalDateTime> toCaptor;

    @Test
    void test_getStats() {
        when(mockStatisticsRepository.getStats(any(MessageType.class), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(List.of(new StatsEntry(SMS, SMS, SENT)));

        var from = LocalDate.of(2023, 3, 27);
        var to = LocalDate.of(2023, 3, 28);

        var result = mockStatisticsRepository.getStats(SMS, from, to);

        assertThat(result).isNotNull().hasSize(1);

        verify(mockStatisticsRepository, times(1)).getStats(eq(SMS), fromCaptor.capture(), toCaptor.capture());

        assertThat(fromCaptor.getValue().toLocalDate()).isEqualTo(from);
        assertThat(toCaptor.getValue()).satisfies(capturedTo -> {
            assertThat(capturedTo.toLocalDate()).isEqualTo(to);
            assertThat(capturedTo.getHour()).isEqualTo(23);
            assertThat(capturedTo.getMinute()).isEqualTo(59);
            assertThat(capturedTo.getSecond()).isEqualTo(59);
        });
    }

    @Test
    void test_getStats_withNullFromAndTo() {
        when(mockStatisticsRepository.getStats(any(MessageType.class), nullable(LocalDateTime.class), nullable(LocalDateTime.class)))
            .thenReturn(List.of(new StatsEntry(EMAIL, EMAIL, FAILED)));

        var result = mockStatisticsRepository.getStats(SMS, (LocalDate) null, null);

        assertThat(result).isNotNull().hasSize(1);

        verify(mockStatisticsRepository, times(1)).getStats(eq(SMS), ArgumentMatchers.<LocalDateTime>isNull(), isNull());
    }
}

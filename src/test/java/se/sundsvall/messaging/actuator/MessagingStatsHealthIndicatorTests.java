package se.sundsvall.messaging.actuator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.entity.CounterEntity;

@ExtendWith(MockitoExtension.class)
class MessagingStatsHealthIndicatorTests {

    @Mock
    private CounterRepository mockCounterRepository;
    @Mock
    private Health.Builder mockHealthBuilder;

    @InjectMocks
    private MessagingStatsHealthIndicator healthIndicator;

    @Test
    void test_doHealthCheck() {
        when(mockCounterRepository.findAll())
            .thenReturn(List.of(createCounter("counter.1"), createCounter("counter.2")));

        healthIndicator.doHealthCheck(mockHealthBuilder);

        verify(mockCounterRepository, times(1)).findAll();
        verify(mockHealthBuilder, times(1)).up();
        verify(mockHealthBuilder, times(2)).withDetail(any(String.class), any());
    }

    @Test
    void test_doHealthCheck_whenNoStatsAreAvailable() {
        when(mockCounterRepository.findAll()).thenReturn(List.of());

        healthIndicator.doHealthCheck(mockHealthBuilder);

        verify(mockCounterRepository, times(1)).findAll();
        verify(mockHealthBuilder, times(1)).unknown();
        verify(mockHealthBuilder, never()).withDetail(any(String.class), any());
    }

    private CounterEntity createCounter(final String name) {
        return CounterEntity.builder()
            .withName(name)
            .build();
    }
}

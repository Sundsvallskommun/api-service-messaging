package se.sundsvall.messaging.actuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessagingStatsHealthIndicatorTest {

	@Mock
	private Health.Builder mockHealthBuilder;

	@InjectMocks
	private MessagingStatsHealthIndicator healthIndicator;

	@Test
	void test_doHealthCheck() {
		// TODO: implement properly when statistics querying is done
		healthIndicator.doHealthCheck(mockHealthBuilder);

		verify(mockHealthBuilder, times(1)).up();
	}
}

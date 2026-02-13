package se.sundsvall.messaging.actuator;

import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

@Component
class MessagingStatsHealthIndicator extends AbstractHealthIndicator {

	@Override
	protected void doHealthCheck(final Health.Builder builder) {
		// TODO: implement properly when statistics querying is done
		builder.up();
	}
}

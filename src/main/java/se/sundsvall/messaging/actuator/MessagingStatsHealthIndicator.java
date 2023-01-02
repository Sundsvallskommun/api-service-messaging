package se.sundsvall.messaging.actuator;

import java.util.Comparator;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.Counter;

@Component
class MessagingStatsHealthIndicator extends AbstractHealthIndicator {

    private final DbIntegration dbIntegration;

    MessagingStatsHealthIndicator(final DbIntegration dbIntegration) {
        this.dbIntegration = dbIntegration;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        var counters = dbIntegration.getAllCounters().stream()
            .sorted(Comparator.comparing(Counter::name))
            .toList();

        if (counters.isEmpty()) {
            builder.unknown();
        } else {
            builder.up();
        }

        counters.forEach(counter -> builder.withDetail(counter.name(), counter.value()));
    }
}

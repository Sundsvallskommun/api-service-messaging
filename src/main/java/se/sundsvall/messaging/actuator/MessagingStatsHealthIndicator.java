package se.sundsvall.messaging.actuator;

import java.util.Comparator;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.entity.CounterEntity;

@Component
class MessagingStatsHealthIndicator extends AbstractHealthIndicator {

    private final CounterRepository counterRepository;

    MessagingStatsHealthIndicator(final CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @Override
    protected void doHealthCheck(final Health.Builder builder) {
        var counters = counterRepository.findAll().stream()
            .sorted(Comparator.comparing(CounterEntity::getName))
            .toList();

        if (counters.isEmpty()) {
            builder.unknown();
        } else {
            builder.up();
        }

        counters.forEach(counter -> builder.withDetail(counter.getName(), counter.getValue()));
    }
}

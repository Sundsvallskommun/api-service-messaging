package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withDepartment;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withFromDataAndToDate;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withOrigin;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withOriginalMessageType;

@Repository
@CircuitBreaker(name = "statisticsRepository")
public interface StatisticsRepository extends JpaRepository<HistoryEntity, Long>, JpaSpecificationExecutor<HistoryEntity> {

    default List<HistoryEntity> getStats(final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return findAll(withOriginalMessageType(messageType.toString())
            .and(withFromDataAndToDate(toFromDate(from), toToDate(to))));
    }

    default List<HistoryEntity> getStatsByOriginAndDepartment(final String origin, final String department, final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return findAll(withOrigin(origin)
            .and(withDepartment(department))
            .and(withOriginalMessageType(messageType.toString()))
            .and(withFromDataAndToDate(toFromDate(from), toToDate(to))));
    }

    private LocalDateTime toFromDate(LocalDate from) {
        return from != null ? from.atStartOfDay() : null;
    }

    private LocalDateTime toToDate(LocalDate to) {
        return to != null ? to.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59) : null;
    }
}

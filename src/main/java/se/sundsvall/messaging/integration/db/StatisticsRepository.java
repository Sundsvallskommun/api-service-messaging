package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.MessageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@CircuitBreaker(name = "statisticsRepository")
public interface StatisticsRepository extends JpaRepository<HistoryEntity, Long> {

    default List<StatsEntry> getStats(final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return getStatsQuery(messageType,
            from != null ? from.atStartOfDay() : null,
            to != null ? to.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59) : null);
    }

    @Query("""
        SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status) FROM HistoryEntity h WHERE
        (:message_type IS NULL OR h.originalMessageType = :message_type) AND
        (:from_date IS NULL OR h.createdAt >= :from_date) AND
        (:to_date IS NULL OR h.createdAt <= :to_date)
    """)
    List<StatsEntry> getStatsQuery(
        @Param("message_type") final MessageType messageType,
        @Param("from_date") final LocalDateTime from,
        @Param("to_date") final LocalDateTime to);

    default List<StatsEntry> getStatsByOriginAndDepartment(final String origin, final String department, final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return getStatsByOriginAndDepartmentQuery(origin,
            department,
            messageType,
            from != null ? from.atStartOfDay() : null,
            to != null ? to.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59) : null);
    }

    @Query(value = """
        SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status, h.origin, h.department) FROM HistoryEntity h WHERE
        (:message_type IS NULL OR h.originalMessageType = :message_type) AND
        (:from_date IS NULL OR h.createdAt >= :from_date) AND
        (:to_date IS NULL OR h.createdAt <= :to_date) AND
        (:department IS NULL OR h.department = :department) AND
        (:origin IS NULL OR h.origin = :origin)
    """)
    List<StatsEntry>  getStatsByOriginAndDepartmentQuery(
        @Param("origin") final String origin,
        @Param("department") final String department,
        @Param("message_type") final MessageType messageType,
        @Param("from_date") final LocalDateTime from,
        @Param("to_date") final LocalDateTime to);
}

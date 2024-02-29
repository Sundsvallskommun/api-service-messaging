package se.sundsvall.messaging.integration.db;

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
public interface StatisticsRepository extends JpaRepository<HistoryEntity, Long> {

    default List<StatsEntry> getStats(final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return getStats(messageType,
            from != null ? from.atStartOfDay() : null,
            to != null ? to.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59) : null);
    }

    @Query("""
        SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status) FROM HistoryEntity h WHERE
        (:message_type IS NULL OR h.originalMessageType = :message_type) AND
        (:from_date IS NULL OR h.createdAt >= :from_date) AND
        (:to_date IS NULL OR h.createdAt <= :to_date)
    """)
    List<StatsEntry> getStats(
        @Param("message_type") final MessageType messageType,
        @Param("from_date") final LocalDateTime from,
        @Param("to_date") final LocalDateTime to);

    default List<StatsEntry> getStatsByDepartment(final String department, final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return getStatsByDepartmentQuery(department,
            messageType,
            from != null ? from.atStartOfDay() : null,
            to != null ? to.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59) : null);
    }

    @Query(value = """
        SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status, CAST(JSON_UNQUOTE(JSON_EXTRACT(h.content, '$.department')) as string)) FROM HistoryEntity h WHERE
        (:message_type IS NULL OR h.originalMessageType = :message_type) AND
        (:from_date IS NULL OR h.createdAt >= :from_date) AND
        (:to_date IS NULL OR h.createdAt <= :to_date) AND
        (:department IS NULL OR FUNCTION('JSON_EXTRACT', h.content, '$.department') = :department)
    """)
    List<StatsEntry>  getStatsByDepartmentQuery(
        @Param("department") final String department,
        @Param("message_type") final MessageType messageType,
        @Param("from_date") final LocalDateTime from,
        @Param("to_date") final LocalDateTime to);
}

package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.MessageType;

@Repository
@CircuitBreaker(name = "statisticsRepository")
public interface StatisticsRepository extends JpaRepository<HistoryEntity, Long> {

	default List<StatsEntry> getStats(final MessageType messageType, final LocalDate from,
		final LocalDate to, final String municipalityId) {
		return getStatsQuery(messageType, startOfDay(from), endOfDay(to), municipalityId);
	}

	@Query("""
		SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status, h.municipalityId) FROM HistoryEntity h WHERE
		(:message_type IS NULL OR h.originalMessageType = :message_type) AND
		(:from_date IS NULL OR h.createdAt >= :from_date) AND
		(:to_date IS NULL OR h.createdAt <= :to_date) AND
		(:municipality_id IS NULL OR h.municipalityId = :municipality_id)
		""")
	List<StatsEntry> getStatsQuery(
		@Param("message_type") final MessageType messageType,
		@Param("from_date") final LocalDateTime from,
		@Param("to_date") final LocalDateTime to,
		@Param("municipality_id") final String municipalityId);

	default List<StatsEntry> getStatsByMunicipalityIdAndyOriginAndDepartment(final String municipalityId, final String origin, final String department, final MessageType messageType, final LocalDate from, final LocalDate to) {
		return getStatsByOriginAndDepartmentQuery(origin, department, messageType, startOfDay(from), endOfDay(to), municipalityId);
	}

	@Query(value = """
		SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status, h.origin, h.department, h.municipalityId) FROM HistoryEntity h WHERE
		(:message_type IS NULL OR h.originalMessageType = :message_type) AND
		(:from_date IS NULL OR h.createdAt >= :from_date) AND
		(:to_date IS NULL OR h.createdAt <= :to_date) AND
		(:department IS NULL OR h.department = :department) AND
		(:origin IS NULL OR h.origin = :origin) AND
		(:municipality_id IS NULL OR h.municipalityId = :municipality_id)
		""")
	List<StatsEntry> getStatsByOriginAndDepartmentQuery(
		@Param("origin") final String origin,
		@Param("department") final String department,
		@Param("message_type") final MessageType messageType,
		@Param("from_date") final LocalDateTime from,
		@Param("to_date") final LocalDateTime to,
		@Param("municipality_id") final String municipalityId);

	default List<StatsEntry> getStatsByMunicipalityIdAndDepartmentAndOriginAndMessageTypes(final String municipalityId, final String department, final String origin, final List<MessageType> messageTypes, final LocalDate from, final LocalDate to) {
		return getStatsByMunicipalityIdAndDepartmentAndOriginAndMessageTypesQuery(municipalityId, department, origin, messageTypes, startOfDay(from), endOfDay(to));
	}

	@Query(value = """
		SELECT NEW StatsEntry(h.messageType, h.originalMessageType, h.status, h.origin, h.department, h.municipalityId) FROM HistoryEntity h WHERE
		(:message_types IS NULL OR h.originalMessageType IN :message_types) AND
		(:from_date IS NULL OR h.createdAt >= :from_date) AND
		(:to_date IS NULL OR h.createdAt <= :to_date) AND
		(:department IS NULL OR h.department = :department) AND
		(:origin IS NULL OR h.origin = :origin) AND
		(:municipality_id IS NULL OR h.municipalityId = :municipality_id)
		""")
	List<StatsEntry> getStatsByMunicipalityIdAndDepartmentAndOriginAndMessageTypesQuery(
		@Param("municipality_id") final String municipalityId,
		@Param("department") final String department,
		@Param("origin") final String origin,
		@Param("message_types") final List<MessageType> messageTypes,
		@Param("from_date") final LocalDateTime from,
		@Param("to_date") final LocalDateTime to);

	default LocalDateTime startOfDay(LocalDate date) {
		return Optional.ofNullable(date)
			.map(LocalDate::atStartOfDay)
			.orElse(null);
	}

	default LocalDateTime endOfDay(LocalDate date) {
		return Optional.ofNullable(date)
			.map(d -> d.atStartOfDay().plusHours(23).plusMinutes(59).plusSeconds(59))
			.orElse(null);
	}

}

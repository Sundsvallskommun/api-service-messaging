package se.sundsvall.messaging.integration.db;

import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withCreatedAtAfter;
import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withCreatedAtBefore;
import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withDepartment;
import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withMunicipalityId;
import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withOrigin;
import static se.sundsvall.messaging.integration.db.specification.StatisticsSpecification.withOriginalMessageTypeIn;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.StatsProjection;
import se.sundsvall.messaging.model.MessageType;

@Repository
@CircuitBreaker(name = "statisticsRepository")
public interface StatisticsRepository extends JpaRepository<HistoryEntity, Long>, JpaSpecificationExecutor<HistoryEntity> {

	default List<StatsProjection> findAllByParameters(final String municipalityId, final String origin, final String department, final List<MessageType> messageTypes, final LocalDate from, final LocalDate to) {
		var specification = Specification
			.where(withMunicipalityId(municipalityId))
			.and(withOrigin(origin))
			.and(withDepartment(department))
			.and(withOriginalMessageTypeIn(messageTypes))
			.and(withCreatedAtAfter(startOfDay(from)))
			.and(withCreatedAtBefore(endOfDay(to)));

		return findBy(specification, query -> query.as(StatsProjection.class).all());
	}

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

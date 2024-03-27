package se.sundsvall.messaging.integration.db.specification;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.nonNull;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.CREATED_AT;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.DEPARTMENT;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.ORIGIN;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.ORIGINAL_MESSAGE_TYPE;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.PARTY_ID;

public interface HistorySpecification {

	Specification<HistoryEntity> FALLBACK = (root, query, cb) -> cb.equal(cb.literal(TRUE), TRUE);

	static Specification<HistoryEntity> withOrigin(String origin) {
		return equalFilter(ORIGIN, origin);
	}

	static Specification<HistoryEntity> withDepartment(String department) {
		return equalFilter(DEPARTMENT, department);
	}

	static Specification<HistoryEntity> withOriginalMessageType(String originalMessageType) {
		return equalFilter(ORIGINAL_MESSAGE_TYPE, originalMessageType);
	}

	static Specification<HistoryEntity> withPartyId(final String partyId) {
		return (entity, cq, cb) -> cb.equal(entity.get(PARTY_ID), partyId);
	}

	static Specification<HistoryEntity> withCreatedAtBefore(final LocalDate when) {
		if (when == null) {
			return FALLBACK;
		}

		return (entity, cq, cb) -> cb.lessThanOrEqualTo(entity.get(CREATED_AT), when.atStartOfDay());
	}

	static Specification<HistoryEntity> withCreatedAtAfter(final LocalDate when) {
		if (when == null) {
			return FALLBACK;
		}

		return (entity, cq, cb) -> cb.greaterThanOrEqualTo(entity.get(CREATED_AT), when.atStartOfDay().plusDays(1));
	}

	static Specification<HistoryEntity> orderByCreatedAtDesc(final Specification<HistoryEntity> specification) {
		return (entity, cq, cb) -> {
			cq.orderBy(cb.desc(entity.get(CREATED_AT)));

			return specification.toPredicate(entity, cq, cb);
		};
	}

	static Specification<HistoryEntity> withFromDataAndToDate(LocalDateTime dateFrom, LocalDateTime dateTo) {
		return dateFilter(CREATED_AT, dateFrom, dateTo);
	}

	private static Specification<HistoryEntity> equalFilter(String attribute, String value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.equal(cb.upper(entity.get(attribute)), value.toUpperCase()) : cb.and();
	}

	private static Specification<HistoryEntity> dateFilter(String attribute, LocalDateTime dateFrom, LocalDateTime dateTo) {
		return (entity, cq, cb) -> {

			if (nonNull(dateFrom) && nonNull(dateTo) ) {
				return cb.between(entity.get(attribute), dateFrom, dateTo);
			} else if (nonNull(dateFrom)) {
				return cb.greaterThanOrEqualTo(entity.get(attribute), dateFrom);
			} else if (nonNull(dateTo)) {
				return cb.lessThanOrEqualTo(entity.get(attribute), dateTo);
			}

			// always-true predicate, meaning that if no dateFrom or to has been set, no filtering will be applied
			return cb.and();
		};
	}
}

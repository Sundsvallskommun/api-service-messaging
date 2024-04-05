package se.sundsvall.messaging.integration.db.specification;

import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

import java.time.LocalDate;

import static java.lang.Boolean.TRUE;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.CREATED_AT;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.PARTY_ID;

public interface HistorySpecification {

	Specification<HistoryEntity> FALLBACK = (root, query, cb) -> cb.equal(cb.literal(TRUE), TRUE);

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
}

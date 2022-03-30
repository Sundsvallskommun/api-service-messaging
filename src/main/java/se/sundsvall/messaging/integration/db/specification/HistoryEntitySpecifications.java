package se.sundsvall.messaging.integration.db.specification;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity_;
import se.sundsvall.messaging.integration.db.specification.support.LocalDateTimeQuery;
import se.sundsvall.messaging.integration.db.specification.support.Spec;
import se.sundsvall.messaging.integration.db.specification.support.StringQuery;

public final class HistoryEntitySpecifications {

    private HistoryEntitySpecifications() { }

    public static Specification<HistoryEntity> withBatchId(final String batchId) {
        return new Spec<>(StringQuery.equalTo(HistoryEntity_.batchId, batchId));
    }

    public static Specification<HistoryEntity> withPartyId(final String partyId) {
        return new Spec<>(StringQuery.equalTo(HistoryEntity_.partyId, partyId));
    }

    public static Specification<HistoryEntity> withCreatedAtBetween(final LocalDate from, final LocalDate to) {
        return withCreatedAtAfter(from).and(withCreatedAtBefore(to));
    }

    public static Specification<HistoryEntity> withCreatedAtAfter(final LocalDate when) {
        return new Spec<>(LocalDateTimeQuery.gte(HistoryEntity_.createdAt, when != null ? when.atStartOfDay() : null));
    }

    public static Specification<HistoryEntity> withCreatedAtBefore(final LocalDate when) {
        return new Spec<>(LocalDateTimeQuery.lte(HistoryEntity_.createdAt, when != null ? when.atStartOfDay().plusHours(24) : null));
    }
}

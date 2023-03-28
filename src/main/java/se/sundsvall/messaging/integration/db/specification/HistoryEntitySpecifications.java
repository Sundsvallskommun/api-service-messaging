package se.sundsvall.messaging.integration.db.specification;

import static java.lang.Boolean.TRUE;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.CREATED_AT;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.MESSAGE_TYPE;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.PARTY_ID;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.STATUS;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public final class HistoryEntitySpecifications {

    private static final Specification<HistoryEntity> FALLBACK = (root, query, cb) -> cb.equal(cb.literal(TRUE), TRUE);

    private HistoryEntitySpecifications() { }

    public static Specification<HistoryEntity> any() {
        return FALLBACK;
    }

    public static Specification<HistoryEntity> withPartyId(final String partyId) {
        return (root, query, cb) -> cb.equal(root.get(PARTY_ID), partyId);
    }

    public static Specification<HistoryEntity> withMessageType(final MessageType messageType) {
        if (messageType == null) {
            return FALLBACK;
        }

        return (root, query, cb) -> cb.equal(root.get(MESSAGE_TYPE), messageType);
    }

    public static Specification<HistoryEntity> withStatus(final MessageStatus status) {
        if (status == null) {
            return FALLBACK;
        }

        return (root, query, cb) -> cb.equal(root.get(STATUS), status);
    }

    public static Specification<HistoryEntity> withCreatedAtBefore(final LocalDate when) {
        if (when == null) {
            return FALLBACK;
        }

        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(CREATED_AT), when.atStartOfDay());
    }

    public static Specification<HistoryEntity> withCreatedAtAfter(final LocalDate when) {
        if (when == null) {
            return FALLBACK;
        }

        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), when.atStartOfDay().plusDays(1));
    }
}

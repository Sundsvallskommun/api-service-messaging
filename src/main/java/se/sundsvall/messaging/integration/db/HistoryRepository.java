package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.CREATED_AT;
import static se.sundsvall.messaging.integration.db.entity.HistoryEntity_.PARTY_ID;

@Repository
@CircuitBreaker(name = "historyRepository")
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>,
        JpaSpecificationExecutor<HistoryEntity> {

    Optional<HistoryEntity> findByDeliveryId(String deliveryId);

    List<HistoryEntity> findByMessageId(String messageId);

    List<HistoryEntity> findByBatchId(String batchId);

    interface Specs {

        Specification<HistoryEntity> FALLBACK = (root, query, cb) -> cb.equal(cb.literal(TRUE), TRUE);

        static Specification<HistoryEntity> withPartyId(final String partyId) {
            return (root, query, cb) -> cb.equal(root.get(PARTY_ID), partyId);
        }

        static Specification<HistoryEntity> withCreatedAtBefore(final LocalDate when) {
            if (when == null) {
                return FALLBACK;
            }

            return (root, query, cb) -> cb.lessThanOrEqualTo(root.get(CREATED_AT), when.atStartOfDay());
        }

        static Specification<HistoryEntity> withCreatedAtAfter(final LocalDate when) {
            if (when == null) {
                return FALLBACK;
            }

            return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get(CREATED_AT), when.atStartOfDay().plusDays(1));
        }

        static Specification<HistoryEntity> orderByCreatedAtDesc(final Specification<HistoryEntity> specification) {
            return (root, query, cb) -> {
                query.orderBy(cb.desc(root.get(CREATED_AT)));

                return specification.toPredicate(root, query, cb);
            };
        }
    }
}

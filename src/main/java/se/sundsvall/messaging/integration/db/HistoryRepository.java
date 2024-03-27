package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

import java.util.List;
import java.util.Optional;

@Repository
@CircuitBreaker(name = "historyRepository")
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>,
        JpaSpecificationExecutor<HistoryEntity> {

    Optional<HistoryEntity> findByDeliveryId(String deliveryId);

    List<HistoryEntity> findByMessageId(String messageId);

    List<HistoryEntity> findByBatchId(String batchId);
}

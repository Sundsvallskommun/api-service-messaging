package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>,
        JpaSpecificationExecutor<HistoryEntity> {

    Optional<HistoryEntity> findByDeliveryId(String deliveryId);

    List<HistoryEntity> findByMessageId(String messageId);

    List<HistoryEntity> findByBatchId(String batchId);
}

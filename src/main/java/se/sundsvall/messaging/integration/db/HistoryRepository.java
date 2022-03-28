package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, String> {

    Optional<HistoryEntity> findByMessageIdEquals(String messageId);

    List<HistoryEntity> findByBatchIdEquals(String batchId);

    List<HistoryEntity> findByPartyIdEquals(String partyId);
}

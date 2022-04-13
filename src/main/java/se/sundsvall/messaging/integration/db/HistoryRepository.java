package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryEntity, String>,
        JpaSpecificationExecutor<HistoryEntity> {

    List<HistoryEntity> findByBatchIdEquals(String batchId);

    List<HistoryEntity> findByPartyIdEquals(String partyId);
}

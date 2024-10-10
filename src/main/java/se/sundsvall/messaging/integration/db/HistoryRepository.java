package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

@Repository
@CircuitBreaker(name = "historyRepository")
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>, JpaSpecificationExecutor<HistoryEntity> {

	Optional<HistoryEntity> findByMunicipalityIdAndDeliveryId(String municipalityId, String deliveryId);

	List<HistoryEntity> findByMunicipalityIdAndMessageId(String municipalityId, String messageId);

	List<HistoryEntity> findByMunicipalityIdAndBatchId(String municipalityId, String batchId);

	Page<HistoryEntity> findByMunicipalityIdAndIssuer(String municipalityId, String issuer, Pageable pageable);
}

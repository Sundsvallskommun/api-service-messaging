package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageStatus;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;

@Repository
@CircuitBreaker(name = "historyRepository")
public interface HistoryRepository extends JpaRepository<HistoryEntity, Long>, PagingAndSortingRepository<HistoryEntity, Long>, JpaSpecificationExecutor<HistoryEntity> {

	Optional<HistoryEntity> findByMunicipalityIdAndDeliveryId(String municipalityId, String deliveryId);

	List<HistoryEntity> findByMunicipalityIdAndMessageId(String municipalityId, String messageId);

	List<HistoryEntity> findByMunicipalityIdAndBatchId(String municipalityId, String batchId);

	Page<HistoryEntity> findByMunicipalityIdAndIssuer(String municipalityId, String issuer, Pageable pageable);

	@Query("SELECT DISTINCT h.messageId FROM HistoryEntity h WHERE h.municipalityId = :municipalityId AND h.issuer = :userId")
	Page<String> findDistinctMessageIdsByMunicipalityIdAndUserId(
		@Param("municipalityId") final String municipalityId,
		@Param("userId") final String userId,
		final Pageable pageable);

	List<HistoryEntity> findByMunicipalityIdAndMessageIdAndStatus(String municipalityId, String messageId, MessageStatus status);

	Optional<HistoryEntity> findFirstByMunicipalityIdAndMessageId(String municipalityId, String messageId);
}

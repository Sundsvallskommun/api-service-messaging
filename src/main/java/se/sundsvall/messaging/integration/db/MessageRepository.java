package se.sundsvall.messaging.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;

@CircuitBreaker(name = "messageRepository")
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

	boolean existsByBatchId(String batchId);

	Optional<MessageEntity> findByDeliveryId(String deliveryId);

	boolean existsByMessageId(String messageId);

	@Transactional
	void deleteByDeliveryId(String deliveryId);

	List<MessageEntity> findByStatusOrderByCreatedAtAsc(MessageStatus status);
}

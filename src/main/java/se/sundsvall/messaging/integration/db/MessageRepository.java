package se.sundsvall.messaging.integration.db;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    Optional<MessageEntity> findByDeliveryId(String deliveryId);

    boolean existsByMessageId(String messageId);

    @Transactional
    void deleteByDeliveryId(String deliveryId);

    @Query("from MessageEntity me where me.status = :status order by me.createdAt asc")
    List<MessageEntity> findLatestWithStatus(@Param("status") final MessageStatus status);
}

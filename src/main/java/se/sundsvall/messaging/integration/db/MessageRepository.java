package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public interface MessageRepository extends JpaRepository<MessageEntity, String> {
    
    @Query("from MessageEntity me where me.status = :status order by me.createdAt asc")
    List<MessageEntity> findLatestWithStatus(@Param("status") final MessageStatus status);

    @Query("from MessageEntity me where me.type = :type and me.status = :status order by me.createdAt asc")
    List<MessageEntity> findLatestWithTypeAndStatus(
        @Param("type") final MessageType type,
        @Param("status") final MessageStatus status);
}

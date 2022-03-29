package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.WebMessageEntity;
import se.sundsvall.messaging.model.MessageStatus;

@Repository
public interface WebMessageRepository extends JpaRepository<WebMessageEntity, String> {

    @Query("from WebMessageEntity wme where wme.status = :status order by wme.createdAt asc")
    List<WebMessageEntity> findLatestWithStatus(@Param("status") MessageStatus status);
}

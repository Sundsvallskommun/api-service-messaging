package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.MessageStatus;

@Repository
public interface SmsRepository extends JpaRepository<SmsEntity, String> {

    @Query("from SmsEntity se where se.status = :status order by se.createdAt asc")
    List<SmsEntity> findLatestWithStatus(@Param("status") MessageStatus status);
}

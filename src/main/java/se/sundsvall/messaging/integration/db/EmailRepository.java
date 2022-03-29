package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.model.MessageStatus;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, String> {

    @Query("from EmailEntity ee where ee.status = :status order by ee.createdAt asc")
    List<EmailEntity> findLatestWithStatus(@Param("status") MessageStatus status);
}

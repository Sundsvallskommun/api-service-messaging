package se.sundsvall.messaging.integration.db;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.MessageStatus;

@Repository
public interface SmsRepository extends JpaRepository<SmsEntity, String> {

    List<SmsEntity> findByStatusEquals(MessageStatus status, Sort sort);
}

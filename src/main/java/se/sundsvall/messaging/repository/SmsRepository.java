package se.sundsvall.messaging.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.model.entity.SmsEntity;

@Repository
public interface SmsRepository extends JpaRepository<SmsEntity, String> {

    List<SmsEntity> findByStatusEquals(MessageStatus status, Sort sort);
}

package se.sundsvall.messaging.repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.model.entity.EmailEntity;

@Repository
public interface EmailRepository extends JpaRepository<EmailEntity, String> {

    List<EmailEntity> findByStatusEquals(MessageStatus status, Sort sort);
}

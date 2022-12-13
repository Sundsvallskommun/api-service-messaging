package se.sundsvall.messaging.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import se.sundsvall.messaging.integration.db.entity.AllowedRecipientEntity;
import se.sundsvall.messaging.model.MessageType;

public interface AllowedRecipientRepository
        extends JpaRepository<AllowedRecipientEntity, AllowedRecipientEntity.Id> {

    default Optional<AllowedRecipientEntity> findByMessageTypeAndRecipient(
            final MessageType messageType, final String recipient) {
        return findById(AllowedRecipientEntity.Id.builder()
            .withMessageType(messageType)
            .withRecipient(recipient)
            .build());
    }
}

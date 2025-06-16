package se.sundsvall.messaging.integration.db.projection;

import java.time.LocalDateTime;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

/**
 * Interface used when reading data to build batch history response
 */

public interface BatchHistoryProjection {

	String getBatchId();

	String getMessageId();

	MessageType getMessageType();

	MessageType getOriginalMessageType();

	LocalDateTime getCreatedAt();

	MessageStatus getStatus();
}

package se.sundsvall.messaging.integration.db.projection;

import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

public interface StatsProjection {

	MessageType getMessageType();

	MessageType getOriginalMessageType();

	MessageStatus getStatus();

	String getOrigin();

	String getDepartment();

	String getMunicipalityId();

}

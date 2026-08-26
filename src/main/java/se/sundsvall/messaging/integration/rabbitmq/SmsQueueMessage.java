package se.sundsvall.messaging.integration.rabbitmq;

import se.sundsvall.messaging.api.model.request.Priority;

public record SmsQueueMessage(
	String municipalityId,
	String sender,
	String mobileNumber,
	String message,
	Priority priority) {
}

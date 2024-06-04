package se.sundsvall.messaging.integration.smssender;

import lombok.Builder;
import se.sundsvall.messaging.api.model.request.Priority;

@Builder(setterPrefix = "with")
public record SmsDto(
	String sender,
	String mobileNumber,
	String message,
	Priority priority) {
}

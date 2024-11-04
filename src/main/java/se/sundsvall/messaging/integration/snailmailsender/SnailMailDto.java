package se.sundsvall.messaging.integration.snailmailsender;

import lombok.Builder;
import se.sundsvall.messaging.api.model.request.Address;

import java.util.List;

@Builder(setterPrefix = "with")
public record SnailMailDto(
	String partyId,
	Address address,
	String batchId,
	String department,
	String deviation,
	List<Attachment> attachments) {

	@Builder(setterPrefix = "with")
	public record Attachment(
		String name,
		String contentType,
		String content) {
	}

}

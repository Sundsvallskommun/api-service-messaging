package se.sundsvall.messaging.integration.snailmailsender;

import java.util.List;
import lombok.Builder;
import se.sundsvall.messaging.model.Address;

@Builder(setterPrefix = "with")
public record SnailMailDto(
	String partyId,
	Address address,
	String batchId,
	String department,
	String deviation,
	String sentBy,
	String origin,
	List<Attachment> attachments) {

	@Builder(setterPrefix = "with")
	public record Attachment(
		String filename,
		String contentType,
		String content) {
	}

}

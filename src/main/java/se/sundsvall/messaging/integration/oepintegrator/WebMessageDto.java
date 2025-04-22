package se.sundsvall.messaging.integration.oepintegrator;

import java.util.List;
import lombok.Builder;
import se.sundsvall.messaging.model.ExternalReference;

@Builder(setterPrefix = "with")
public record WebMessageDto(
	String partyId,
	List<ExternalReference> externalReferences,
	String message,
	String userId,
	String oepInstance,
	boolean sendAsOwner,
	List<Attachment> attachments) {

	@Builder(setterPrefix = "with")
	public record Attachment(
		String fileName,
		String mimeType,
		String base64Data) {
	}
}

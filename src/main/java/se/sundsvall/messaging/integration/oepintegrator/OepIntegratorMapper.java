package se.sundsvall.messaging.integration.oepintegrator;

import generated.se.sundsvall.oepintegrator.ExternalReference;
import generated.se.sundsvall.oepintegrator.Sender;
import generated.se.sundsvall.oepintegrator.WebmessageRequest;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;

@Component
class OepIntegratorMapper {

	WebmessageRequest toWebmessageRequest(final WebMessageDto dto) {
		return Optional.ofNullable(dto)
			.map(webMessageDto -> new WebmessageRequest()
				.externalReferences(toExternalReferences(webMessageDto.externalReferences()))
				.message(dto.message())
				.sender(new Sender()
					.partyId(dto.sendAsOwner() ? null : dto.partyId())
					.userId(dto.sendAsOwner() ? null : dto.userId())
					.administratorId(dto.sendAsOwner() ? findNonNull(dto.userId(), dto.partyId()) : null)))
			.orElse(null);
	}

	String findNonNull(final String userId, final String partyId) {
		if (userId != null) {
			return userId;
		}
		return partyId;
	}

	List<ExternalReference> toExternalReferences(final List<se.sundsvall.messaging.model.ExternalReference> externalReferences) {
		return Optional.ofNullable(externalReferences)
			.map(references -> references.stream()
				.map(this::toExternalReference)
				.toList())
			.orElse(null);
	}

	ExternalReference toExternalReference(final se.sundsvall.messaging.model.ExternalReference externalReference) {
		return Optional.ofNullable(externalReference)
			.map(reference -> new ExternalReference()
				.key(reference.key())
				.value(reference.value()))
			.orElse(null);
	}

	List<AttachmentMultipartFile> toAttachmentMultipartFiles(final List<WebMessageRequest.Attachment> attachments) {
		return attachments.stream()
			.map(this::toAttachmentMultipartFile)
			.toList();
	}

	AttachmentMultipartFile toAttachmentMultipartFile(final WebMessageRequest.Attachment attachment) {
		return Optional.ofNullable(attachment).map(attachment1 -> new AttachmentMultipartFile(
			attachment1.fileName(),
			attachment1.mimeType(),
			toInputStream(attachment1.base64Data())))
			.orElse(null);
	}

	ByteArrayInputStream toInputStream(final String base64Data) {
		return new ByteArrayInputStream(Base64.getDecoder().decode(base64Data));
	}

}

package se.sundsvall.messaging.integration.webmessagesender;

import java.util.List;

import se.sundsvall.messaging.model.ExternalReference;

import lombok.Builder;

@Builder(setterPrefix = "with")
public record WebMessageDto(
        String partyId,
        List<ExternalReference> externalReferences,
        String message,
        List<Attachment> attachments) {

    @Builder(setterPrefix = "with")
    public record Attachment(
        String fileName,
        String mimeType,
        String base64Data) { }
}

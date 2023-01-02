package se.sundsvall.messaging.integration.webmessagesender;

import java.util.Optional;

import org.springframework.stereotype.Component;

import generated.se.sundsvall.webmessagesender.Attachment;
import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import generated.se.sundsvall.webmessagesender.ExternalReference;

@Component
class WebMessageSenderIntegrationMapper {

    CreateWebMessageRequest toCreateWebMessageRequest(final WebMessageDto dto) {
        if (dto == null) {
            return null;
        }

        return new CreateWebMessageRequest()
            .partyId(dto.partyId())
            .externalReferences(Optional.ofNullable(dto.externalReferences())
                .map(externalReferences -> externalReferences.stream()
                    .map(externalReference -> new ExternalReference()
                        .key(externalReference.key())
                        .value(externalReference.value()))
                    .toList())
                .orElse(null))
            .message(dto.message())
            .attachments(Optional.ofNullable(dto.attachments())
                .map(attachments -> attachments.stream()
                    .map(attachment -> new Attachment()
                        .fileName(attachment.fileName())
                        .mimeType(attachment.mimeType())
                        .base64Data(attachment.base64Data()))
                    .toList())
                .orElse(null));
    }
}

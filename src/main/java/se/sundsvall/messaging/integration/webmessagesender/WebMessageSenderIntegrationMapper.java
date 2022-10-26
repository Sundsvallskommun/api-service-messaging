package se.sundsvall.messaging.integration.webmessagesender;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.model.Party;

import generated.se.sundsvall.webmessagesender.Attachment;
import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import generated.se.sundsvall.webmessagesender.ExternalReference;

@Component
class WebMessageSenderIntegrationMapper {

    CreateWebMessageRequest toCreateWebMessageRequest(final WebMessageDto webMessageDto) {
        if (webMessageDto == null) {
            return null;
        }

        List<Attachment> attachments = null;
        if (!CollectionUtils.isEmpty(webMessageDto.getAttachments())) {
            attachments = Optional.of(webMessageDto.getAttachments()).stream()
                    .flatMap(Collection::stream)
                    .map(attachment -> new Attachment()
                            .fileName(attachment.getFileName())
                            .mimeType(attachment.getMimeType())
                            .base64Data(attachment.getBase64Data()))
                    .toList();
        }
        
        List<ExternalReference> externalReferences = null;
        if (!CollectionUtils.isEmpty(webMessageDto.getParty().getExternalReferences())) {
            externalReferences = Optional.ofNullable(webMessageDto.getParty())
                    .map(Party::getExternalReferences)
                    .stream()
                    .flatMap(Collection::stream)
                    .map(externalReference -> new ExternalReference()
                            .key(externalReference.getKey())
                            .value(externalReference.getValue()))
                    .toList();
        }

        return new CreateWebMessageRequest()
                .partyId(Optional.ofNullable(webMessageDto.getParty())
                        .map(Party::getPartyId)
                        .orElse(null))
                .externalReferences(externalReferences)
                .message(webMessageDto.getMessage())
                .attachments(attachments);
    }

}

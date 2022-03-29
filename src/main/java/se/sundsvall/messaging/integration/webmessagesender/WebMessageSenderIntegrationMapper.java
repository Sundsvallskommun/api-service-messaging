package se.sundsvall.messaging.integration.webmessagesender;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.model.Party;

import generated.se.sundsvall.webmessagesender.CreateWebMessageRequest;
import generated.se.sundsvall.webmessagesender.ExternalReference;

@Component
class WebMessageSenderIntegrationMapper {

    CreateWebMessageRequest toCreateWebMessageRequest(final WebMessageDto webMessageDto) {
        if (webMessageDto == null) {
            return null;
        }

        return new CreateWebMessageRequest()
            .partyId(Optional.ofNullable(webMessageDto.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .externalReferences(Optional.ofNullable(webMessageDto.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .map(externalReference -> new ExternalReference()
                    .key(externalReference.getKey())
                    .value(externalReference.getValue()))
                .toList())
            .message(webMessageDto.getMessage());
    }
}

package se.sundsvall.messaging.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.WebMessageDto;
import se.sundsvall.messaging.integration.db.entity.WebMessageEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

public final class WebMessageMapper {

    private WebMessageMapper() { }

    public static WebMessageEntity toEntity(final WebMessageRequest webMessageRequest) {
        if (webMessageRequest == null) {
            return null;
        }

        return WebMessageEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withStatus(MessageStatus.PENDING)
            .withPartyId(Optional.ofNullable(webMessageRequest.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .withExternalReferences(Optional.ofNullable(webMessageRequest.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ExternalReference::getKey, ExternalReference::getValue)))
            .withMessage(webMessageRequest.getMessage())
            .build();
    }

    public static WebMessageDto toDto(final WebMessageEntity webMessageEntity) {
        if (webMessageEntity == null) {
            return null;
        }

        return WebMessageDto.builder()
            .withMessageId(webMessageEntity.getMessageId())
            .withBatchId(webMessageEntity.getBatchId())
            .withStatus(webMessageEntity.getStatus())
            .withParty(Party.builder()
                .withPartyId(webMessageEntity.getPartyId())
                .withExternalReferences(Optional.ofNullable(webMessageEntity.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
            .withMessage(webMessageEntity.getMessage())
            .build();
    }
}

package se.sundsvall.messaging.mapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import se.sundsvall.messaging.api.request.IncomingSmsRequest;
import se.sundsvall.messaging.dto.SmsDto;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

public final class SmsMapper {

    private SmsMapper() { }

    public static SmsEntity toEntity(final IncomingSmsRequest request) {
        if (request == null) {
            return null;
        }

        return SmsEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withSender(request.getSender())
            .withPartyId(Optional.ofNullable(request.getParty()).map(Party::getPartyId).orElse(null))
            .withExternalReferences(Optional.ofNullable(request.getParty())
                .map(Party::getExternalReferences)
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ExternalReference::getKey, ExternalReference::getValue)))
            .withMobileNumber(request.getMobileNumber())
            .withMessage(request.getMessage())
            .withStatus(MessageStatus.PENDING)
            .build();
    }

    public static SmsDto toDto(final SmsEntity entity) {
        if (entity == null) {
            return null;
        }

        return SmsDto.builder()
            .withBatchId(entity.getBatchId())
            .withMessageId(entity.getMessageId())
            .withSender(entity.getSender())
            .withParty(Party.builder()
                .withPartyId(entity.getPartyId())
                .withExternalReferences(Optional.ofNullable(entity.getExternalReferences())
                    .map(Map::entrySet)
                    .orElse(Set.of())
                    .stream()
                    .map(entry -> ExternalReference.builder()
                        .withKey(entry.getKey())
                        .withValue(entry.getValue())
                        .build())
                    .toList())
                .build())
            .withMobileNumber(entity.getMobileNumber())
            .withMessage(entity.getMessage())
            .withStatus(entity.getStatus())
            .build();
    }
}

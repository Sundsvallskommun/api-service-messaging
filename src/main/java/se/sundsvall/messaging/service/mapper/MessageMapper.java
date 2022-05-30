package se.sundsvall.messaging.service.mapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.MessageDto;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

@Component
public class MessageMapper {

    static final Gson GSON = new GsonBuilder().create();

    public MessageDto toMessageDto(final MessageEntity message) {
        return MessageDto.builder()
            .withMessageId(message.getMessageId())
            .withBatchId(message.getBatchId())
            .build();
    }

    public MessageEntity toEntity(final EmailRequest request) {
        var uuid = UUID.randomUUID().toString();

        return MessageEntity.builder()
            .withMessageId(uuid)
            .withPartyId(Optional.ofNullable(request.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .withType(MessageType.EMAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public MessageEntity toEntity(final SmsRequest request) {
        var uuid = UUID.randomUUID().toString();

        return MessageEntity.builder()
            .withMessageId(uuid)
            .withPartyId(Optional.ofNullable(request.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .withType(MessageType.SMS)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public MessageEntity toEntity(final WebMessageRequest request) {
        var uuid = UUID.randomUUID().toString();

        return MessageEntity.builder()
            .withMessageId(uuid)
            .withPartyId(Optional.ofNullable(request.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .withType(MessageType.WEB_MESSAGE)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public List<MessageEntity> toEntities(final DigitalMailRequest request, final String batchId) {
        var uuid = UUID.randomUUID().toString();

        return request.getParty().getPartyIds().stream()
            .map(partyId -> MessageEntity.builder()
                .withMessageId(uuid)
                .withBatchId(batchId)
                .withPartyId(partyId)
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(request))
                .build())
            .toList();
    }

    public MessageEntity toEntity(final String batchId, final MessageRequest.Message request) {
        var uuid = UUID.randomUUID().toString();

        return MessageEntity.builder()
            .withMessageId(uuid)
            .withBatchId(batchId)
            .withPartyId(Optional.ofNullable(request.getParty())
                .map(Party::getPartyId)
                .orElse(null))
            .withType(MessageType.MESSAGE)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }
}

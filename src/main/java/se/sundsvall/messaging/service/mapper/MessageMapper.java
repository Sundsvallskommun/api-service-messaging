package se.sundsvall.messaging.service.mapper;

import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.model.Message;

@Component
public class MessageMapper {

    static final Gson GSON = new GsonBuilder().create();

    public Message toMessage(final EmailRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(EmailRequest.Party::partyId)
                .orElse(null))
            .withType(EMAIL)
            .withOriginalType(EMAIL)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public Message toMessage(final SmsRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(SmsRequest.Party::partyId)
                .orElse(null))
            .withType(SMS)
            .withOriginalType(SMS)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public Message toMessage(final SnailMailRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(SnailMailRequest.Party::partyId)
                .orElse(null))
            .withType(SNAIL_MAIL)
            .withOriginalType(SNAIL_MAIL)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }


    public Message toMessage(final WebMessageRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(WebMessageRequest.Party::partyId)
                .orElse(null))
            .withType(WEB_MESSAGE)
            .withOriginalType(WEB_MESSAGE)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public List<Message> toMessages(final DigitalMailRequest request, final String batchId) {
        var messageId = UUID.randomUUID().toString();

        return request.party().partyIds().stream()
            .map(partyId -> Message.builder()
                .withBatchId(batchId)
                .withMessageId(messageId)
                .withDeliveryId(UUID.randomUUID().toString())
                .withPartyId(partyId)
                .withType(DIGITAL_MAIL)
                .withOriginalType(DIGITAL_MAIL)
                .withStatus(PENDING)
                .withContent(GSON.toJson(request))
                .build())
            .toList();
    }

    public List<Message> toMessages(final LetterRequest request, final String batchId) {
        return request.party().partyIds().stream()
            .map(partyId -> Message.builder()
                .withBatchId(batchId)
                .withMessageId(UUID.randomUUID().toString())
                .withDeliveryId(UUID.randomUUID().toString())
                .withPartyId(partyId)
                .withType(LETTER)
                .withOriginalType(LETTER)
                .withStatus(PENDING)
                .withContent(GSON.toJson(request))
                .build())
            .toList();
    }

    public Message toMessage(final String batchId, final MessageRequest.Message request) {
        var messageId = UUID.randomUUID().toString();

        return Message.builder()
            .withBatchId(batchId)
            .withMessageId(messageId)
            .withDeliveryId(UUID.randomUUID().toString())
            .withPartyId(Optional.ofNullable(request.party())
                .map(MessageRequest.Message.Party::partyId)
                .orElse(null))
            .withType(MESSAGE)
            .withOriginalType(MESSAGE)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }

    public Message toMessage(final SlackRequest request) {
        return Message.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withDeliveryId(UUID.randomUUID().toString())
            .withType(SLACK)
            .withOriginalType(SLACK)
            .withStatus(PENDING)
            .withContent(GSON.toJson(request))
            .build();
    }
}

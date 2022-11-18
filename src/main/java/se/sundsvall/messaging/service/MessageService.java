package se.sundsvall.messaging.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.LetterRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.dto.MessageDto;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingLetterEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
import se.sundsvall.messaging.service.event.IncomingSnailmailEvent;
import se.sundsvall.messaging.service.event.IncomingWebMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;

@Service
public class MessageService {

    private final ApplicationEventPublisher eventPublisher;
    private final MessageRepository repository;
    private final MessageMapper mapper;

    public MessageService(final ApplicationEventPublisher eventPublisher,
            final MessageRepository repository, final MessageMapper mapper) {
        this.eventPublisher = eventPublisher;
        this.repository = repository;
        this.mapper = mapper;
    }

    public MessageBatchDto handleMessageRequest(final MessageRequest request) {
        var batchId = UUID.randomUUID().toString();

        var messages = request.getMessages().stream()
            .map(message -> mapper.toEntity(batchId, message))
            .map(repository::save)
            .toList();

        messages.forEach(message -> eventPublisher.publishEvent(new IncomingMessageEvent(this, message.getId())));

        return MessageBatchDto.builder()
            .withBatchId(batchId)
            .withMessageIds(messages.stream().map(MessageEntity::getMessageId).toList())
            .build();
    }

    public MessageDto handleEmailRequest(final EmailRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingEmailEvent(this, message.getDeliveryId()));

        return mapper.toMessageDto(message);
    }

    public MessageDto handleSmsRequest(final SmsRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingSmsEvent(this, message.getDeliveryId()));

        return mapper.toMessageDto(message);
    }

    public MessageDto handleWebMessageRequest(final WebMessageRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingWebMessageEvent(this, message.getDeliveryId()));

        return mapper.toMessageDto(message);
    }

    public MessageBatchDto handleDigitalMailRequest(final DigitalMailRequest request) {
        var batchId = UUID.randomUUID().toString();

        var entities = repository.saveAll(mapper.toEntities(request, batchId));

        entities.stream()
            .map(MessageEntity::getDeliveryId)
            .forEach(deliveryId -> eventPublisher.publishEvent(new IncomingDigitalMailEvent(this, deliveryId)));

        return MessageBatchDto.builder()
            .withBatchId(batchId)
            .withMessageIds(entities.stream()
                .map(MessageEntity::getMessageId)
                .toList())
            .build();
    }

    public MessageDto handleSnailmailRequest(final SnailmailRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingSnailmailEvent(this, message.getMessageId()));

        return mapper.toMessageDto(message);
    }

    public MessageBatchDto handleLetterRequest(final LetterRequest request) {
        var batchId = UUID.randomUUID().toString();

        var entities = repository.saveAll(mapper.toEntities(request, batchId));

        entities.stream()
                .map(MessageEntity::getDeliveryId)
                .forEach(deliveryId -> eventPublisher.publishEvent(new IncomingLetterEvent(this, deliveryId)));

        return MessageBatchDto.builder()
                .withBatchId(batchId)
                .withMessageIds(entities.stream()
                        .map(MessageEntity::getMessageId)
                        .toList())
                .build();
    }
}

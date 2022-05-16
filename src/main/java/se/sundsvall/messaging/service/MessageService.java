package se.sundsvall.messaging.service;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.dto.MessageDto;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.event.IncomingSmsEvent;
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

    public MessageBatchDto saveMessageRequest(final MessageRequest request) {
        var batchId = UUID.randomUUID().toString();

        var messageIds = request.getMessages().stream()
            .map(message -> mapper.toEntity(batchId, message))
            .map(repository::save)
            .map(mapper::toMessageDto)
            .map(MessageDto::getMessageId)
            .toList();

        messageIds.forEach(messageId -> eventPublisher.publishEvent(new IncomingMessageEvent(this,messageId)));

        return MessageBatchDto.builder()
            .withBatchId(batchId)
            .withMessageIds(messageIds)
            .build();
    }

    public MessageDto saveEmailRequest(final EmailRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingEmailEvent(this, message.getMessageId()));

        return mapper.toMessageDto(message);
    }

    public MessageDto saveSmsRequest(final SmsRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingSmsEvent(this, message.getMessageId()));

        return mapper.toMessageDto(message);
    }

    public MessageDto saveWebMessageRequest(final WebMessageRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingWebMessageEvent(this, message.getMessageId()));

        return mapper.toMessageDto(message);
    }

    public MessageDto saveDigitalMailRequest(final DigitalMailRequest request) {
        var message = repository.save(mapper.toEntity(request));

        eventPublisher.publishEvent(new IncomingDigitalMailEvent(this, message.getMessageId()));

        return mapper.toMessageDto(message);
    }
}

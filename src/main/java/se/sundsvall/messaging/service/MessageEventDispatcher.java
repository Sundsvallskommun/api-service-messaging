package se.sundsvall.messaging.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;

import java.util.UUID;

@Component
public class MessageEventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final BlacklistService blacklistService;
    private final DbIntegration dbIntegration;
    private final MessageMapper messageMapper;

    public MessageEventDispatcher(final ApplicationEventPublisher eventPublisher,
            final BlacklistService blacklistService, final DbIntegration dbIntegration,
            final MessageMapper messageMapper) {
        this.eventPublisher = eventPublisher;
        this.blacklistService = blacklistService;
        this.dbIntegration = dbIntegration;
        this.messageMapper = messageMapper;
    }

    public InternalDeliveryBatchResult handleMessageRequest(final MessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = request.messages().stream()
            .map(message -> messageMapper.toMessage(request.origin(),batchId, message))
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = messages.stream()
            .map(this::publishMessageEvent)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleEmailRequest(final EmailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        return publishMessageEvent(message);
    }

    public InternalDeliveryResult handleSmsRequest(final SmsRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        return publishMessageEvent(message);
    }

    public InternalDeliveryResult handleWebMessageRequest(final WebMessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        return publishMessageEvent(message);
    }

    public InternalDeliveryBatchResult handleDigitalMailRequest(final DigitalMailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(this::publishMessageEvent)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleDigitalInvoiceRequest(final DigitalInvoiceRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        return publishMessageEvent(message);
    }

    public InternalDeliveryBatchResult handleLetterRequest(final LetterRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(this::publishMessageEvent)
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleSlackRequest(final SlackRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        return publishMessageEvent(message);
    }

    private InternalDeliveryResult publishMessageEvent(final Message message) {
        eventPublisher.publishEvent(new IncomingMessageEvent(this, message.type(), message.deliveryId(), message.origin()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), message.type());
    }
}
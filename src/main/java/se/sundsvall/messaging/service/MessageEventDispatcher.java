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

    public InternalDeliveryBatchResult handleMessageRequest(final String origin, final MessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = request.messages().stream()
            .map(message -> messageMapper.toMessage(batchId, message))
            .map(message -> dbIntegration.saveMessage(origin, message))
            .toList();

        var deliveries = messages.stream()
            .map(message -> publishMessageEvent(origin, message))
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleEmailRequest(final String origin, final EmailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(origin, messageMapper.toMessage(request));

        return publishMessageEvent(origin, message);
    }

    public InternalDeliveryResult handleSmsRequest(final String origin, final SmsRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(origin, messageMapper.toMessage(request));

        return publishMessageEvent(origin, message);
    }

    public InternalDeliveryResult handleWebMessageRequest(final String origin, final WebMessageRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(origin, messageMapper.toMessage(request));

        return publishMessageEvent(origin, message);
    }

    public InternalDeliveryBatchResult handleDigitalMailRequest(final String origin, final DigitalMailRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(origin, messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(message -> publishMessageEvent(origin, message))
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleDigitalInvoiceRequest(final String origin, final DigitalInvoiceRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(origin, messageMapper.toMessage(request));

        return publishMessageEvent(origin, message);
    }

    public InternalDeliveryBatchResult handleLetterRequest(final String origin, final LetterRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(origin, messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(message -> publishMessageEvent(origin, message))
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleSlackRequest(final String origin, final SlackRequest request) {
        // Check blacklist
        blacklistService.check(request);

        var message = dbIntegration.saveMessage(origin, messageMapper.toMessage(request));

        return publishMessageEvent(origin, message);
    }

    private InternalDeliveryResult publishMessageEvent(final String origin, final Message message) {
        eventPublisher.publishEvent(new IncomingMessageEvent(this, message.type(), message.deliveryId(), origin));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), message.type());
    }
}
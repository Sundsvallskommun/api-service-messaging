package se.sundsvall.messaging.service;

import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;

@Component
public class MessageEventDispatcher {

    private final ApplicationEventPublisher eventPublisher;
    private final DbIntegration dbIntegration;
    private final MessageMapper messageMapper;

    public MessageEventDispatcher(final ApplicationEventPublisher eventPublisher,
        final DbIntegration dbIntegration, final MessageMapper messageMapper) {
        this.eventPublisher = eventPublisher;
        this.dbIntegration = dbIntegration;
        this.messageMapper = messageMapper;
    }

    public InternalDeliveryBatchResult handleMessageRequest(final MessageRequest request) {
        var batchId = UUID.randomUUID().toString();

        var messages = request.messages().stream()
            .map(message -> messageMapper.toMessage(batchId, message))
            .map(dbIntegration::saveMessage)
            .toList();

        var deliveries = messages.stream()
            .map(message -> {
                eventPublisher.publishEvent(new IncomingMessageEvent(this, MESSAGE, message.deliveryId()));

                return new InternalDeliveryResult(message.messageId(), message.deliveryId(), MESSAGE, null);
            } )
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleEmailRequest(final EmailRequest request) {
        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        eventPublisher.publishEvent(new IncomingMessageEvent(this, EMAIL, message.deliveryId()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), EMAIL, null);
    }

    public InternalDeliveryResult handleSmsRequest(final SmsRequest request) {
        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        eventPublisher.publishEvent(new IncomingMessageEvent(this, SMS, message.deliveryId()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), SMS, null);
    }

    public InternalDeliveryResult handleWebMessageRequest(final WebMessageRequest request) {
        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        eventPublisher.publishEvent(new IncomingMessageEvent(this, WEB_MESSAGE, message.deliveryId()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), WEB_MESSAGE, null);
    }

    public InternalDeliveryBatchResult handleDigitalMailRequest(final DigitalMailRequest request) {
        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(message -> {
                eventPublisher.publishEvent(
                    new IncomingMessageEvent(this, DIGITAL_MAIL, message.deliveryId()));

                return new InternalDeliveryResult(message.messageId(), message.deliveryId(), DIGITAL_MAIL, null);
            })
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleSnailMailRequest(final SnailMailRequest request) {
        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        eventPublisher.publishEvent(new IncomingMessageEvent(this, SNAIL_MAIL, message.deliveryId()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), SNAIL_MAIL, null);
    }

    public InternalDeliveryBatchResult handleLetterRequest(final LetterRequest request) {
        var batchId = UUID.randomUUID().toString();

        var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

        var deliveries = messages.stream()
            .map(message -> {
                eventPublisher.publishEvent(
                    new IncomingMessageEvent(this, LETTER, message.deliveryId()));

                return new InternalDeliveryResult(message.messageId(), message.deliveryId(), LETTER, null);
            })
            .toList();

        return new InternalDeliveryBatchResult(batchId, deliveries);
    }

    public InternalDeliveryResult handleSlackRequest(final SlackRequest request) {
        var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

        eventPublisher.publishEvent(new IncomingMessageEvent(this, SLACK, message.deliveryId()));

        return new InternalDeliveryResult(message.messageId(), message.deliveryId(), SLACK, null);
    }
}
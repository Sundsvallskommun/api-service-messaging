package se.sundsvall.messaging.service;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static se.sundsvall.messaging.api.util.RequestCleaner.cleanSenderName;
import static se.sundsvall.messaging.model.MessageType.SMS;

import java.util.Collections;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingMessageEvent;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;

@Component
public class MessageEventDispatcher {

	private static final Logger LOG = LoggerFactory.getLogger(MessageEventDispatcher.class);

	private final ApplicationEventPublisher eventPublisher;

	private final BlacklistService blacklistService;

	private final DbIntegration dbIntegration;

	private final MessageMapper messageMapper;

	private final RequestMapper requestMapper;

	public MessageEventDispatcher(final ApplicationEventPublisher eventPublisher,
		final BlacklistService blacklistService, final DbIntegration dbIntegration,
		final MessageMapper messageMapper,
		final RequestMapper requestMapper) {
		this.eventPublisher = eventPublisher;
		this.blacklistService = blacklistService;
		this.dbIntegration = dbIntegration;
		this.messageMapper = messageMapper;
		this.requestMapper = requestMapper;
	}

	public InternalDeliveryBatchResult handleMessageRequest(final MessageRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var batchId = UUID.randomUUID().toString();

		final var messages = request.messages().stream()
			.map(message -> messageMapper.toMessage(request.municipalityId(), request.origin(), request.issuer(), batchId, message))
			.map(dbIntegration::saveMessage)
			.toList();

		final var deliveries = messages.stream()
			.map(this::publishMessageEvent)
			.toList();

		return new InternalDeliveryBatchResult(batchId, deliveries, request.municipalityId());
	}

	public InternalDeliveryResult handleEmailRequest(final EmailRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

		return publishMessageEvent(message);
	}

	public InternalDeliveryBatchResult handleEmailBatchRequest(final EmailBatchRequest request) {
		final var batchId = UUID.randomUUID().toString();

		final var deliveryResults = ofNullable(request.parties()).orElse(Collections.emptyList()).stream()
			.filter(party -> isWhitelisted(MessageType.EMAIL, party.emailAddress()))
			.map(party -> requestMapper.toEmailRequest(request, party))
			.map(emailRequest -> messageMapper.toMessage(emailRequest, batchId))
			.map(dbIntegration::saveMessage)
			.map(this::publishMessageEvent)
			.toList();

		return InternalDeliveryBatchResult.builder()
			.withDeliveries(deliveryResults)
			.withBatchId(batchId)
			.withMunicipalityId(request.municipalityId())
			.build();
	}

	public InternalDeliveryBatchResult handleSmsBatchRequest(final SmsBatchRequest request) {
		final var cleanedRequest = request.withSender(cleanSenderName(request.sender()));
		final var batchId = UUID.randomUUID().toString();

		final var deliveryResults = ofNullable(cleanedRequest.parties()).orElse(emptyList()).stream()
			.filter(party -> isWhitelisted(SMS, party.mobileNumber()))
			.map(party -> requestMapper.toSmsRequest(cleanedRequest, party))
			.map(smsRequest -> messageMapper.toMessage(smsRequest, batchId))
			.map(dbIntegration::saveMessage)
			.map(this::publishMessageEvent)
			.toList();

		return InternalDeliveryBatchResult.builder()
			.withDeliveries(deliveryResults)
			.withBatchId(batchId)
			.withMunicipalityId(request.municipalityId())
			.build();
	}

	public InternalDeliveryResult handleSmsRequest(final SmsRequest request) {
		final var cleanedRequest = request.withSender(cleanSenderName(request.sender()));

		// Check blacklist
		blacklistService.check(cleanedRequest);

		final var message = dbIntegration.saveMessage(messageMapper.toMessage(cleanedRequest));

		return publishMessageEvent(message);
	}

	public InternalDeliveryResult handleWebMessageRequest(final WebMessageRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

		return publishMessageEvent(message);
	}

	public InternalDeliveryBatchResult handleDigitalMailRequest(final DigitalMailRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var batchId = UUID.randomUUID().toString();

		final var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

		final var deliveries = messages.stream()
			.map(this::publishMessageEvent)
			.toList();

		return new InternalDeliveryBatchResult(batchId, deliveries, request.municipalityId());
	}

	public InternalDeliveryResult handleDigitalInvoiceRequest(final DigitalInvoiceRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

		return publishMessageEvent(message);
	}

	public InternalDeliveryBatchResult handleLetterRequest(final LetterRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var batchId = UUID.randomUUID().toString();

		final var messages = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId));

		final var deliveries = messages.stream()
			.map(this::publishMessageEvent)
			.toList();

		return new InternalDeliveryBatchResult(batchId, deliveries, request.municipalityId());
	}

	public InternalDeliveryResult handleSlackRequest(final SlackRequest request) {
		// Check blacklist
		blacklistService.check(request);

		final var message = dbIntegration.saveMessage(messageMapper.toMessage(request));

		return publishMessageEvent(message);
	}

	private InternalDeliveryResult publishMessageEvent(final Message message) {
		eventPublisher.publishEvent(new IncomingMessageEvent(this, message.municipalityId(), message.type(), message.deliveryId(), message.origin()));

		return new InternalDeliveryResult(message.messageId(), message.deliveryId(), message.type(), message.municipalityId());
	}

	private boolean isWhitelisted(final MessageType type, final String destination) {
		try {
			blacklistService.check(type, destination);
		} catch (final ThrowableProblem e) {
			LOG.info("Found blacklisted {} destination: {}, skipping.", type, destination);
			// Skipping blacklisted destination
			return false;
		}
		return true;
	}

}

package se.sundsvall.messaging.service;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static se.sundsvall.messaging.api.util.RequestCleaner.cleanSenderName;
import static se.sundsvall.messaging.integration.contactsettings.ContactDto.ContactMethod.NO_CONTACT;
import static se.sundsvall.messaging.integration.contactsettings.ContactDto.ContactMethod.UNKNOWN;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.Mailbox;
import se.sundsvall.messaging.integration.citizen.CitizenIntegration;
import se.sundsvall.messaging.integration.contactsettings.ContactSettingsIntegration;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.oepintegrator.OepIntegratorIntegration;
import se.sundsvall.messaging.integration.slack.SlackIntegration;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.service.mapper.DtoMapper;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;

@Service
public class MessageService {

	private static final Logger LOG = LoggerFactory.getLogger(MessageService.class);

	private final TransactionTemplate transactionTemplate;
	private final DbIntegration dbIntegration;
	private final CitizenIntegration citizenIntegration;
	private final ContactSettingsIntegration contactSettingsIntegration;
	private final SmsSenderIntegration smsSenderIntegration;
	private final EmailSenderIntegration emailSenderIntegration;
	private final DigitalMailSenderIntegration digitalMailSenderIntegration;
	private final SnailMailSenderIntegration snailMailSenderIntegration;
	private final SlackIntegration slackIntegration;
	private final OepIntegratorIntegration oepIntegration;

	private final MessageMapper messageMapper;
	private final RequestMapper requestMapper;
	private final DtoMapper dtoMapper;

	public MessageService(final TransactionTemplate transactionTemplate,
		final DbIntegration dbIntegration,
		final CitizenIntegration citizenIntegration,
		final ContactSettingsIntegration contactSettingsIntegration,
		final SmsSenderIntegration smsSenderIntegration,
		final EmailSenderIntegration emailSenderIntegration,
		final DigitalMailSenderIntegration digitalMailSenderIntegration,
		final SnailMailSenderIntegration snailMailSenderIntegration,
		final SlackIntegration slackIntegration,
		final OepIntegratorIntegration oepIntegration,
		final MessageMapper messageMapper,
		final RequestMapper requestMapper,
		final DtoMapper dtoMapper) {
		this.transactionTemplate = transactionTemplate;
		this.dbIntegration = dbIntegration;
		this.citizenIntegration = citizenIntegration;
		this.contactSettingsIntegration = contactSettingsIntegration;
		this.smsSenderIntegration = smsSenderIntegration;
		this.emailSenderIntegration = emailSenderIntegration;
		this.digitalMailSenderIntegration = digitalMailSenderIntegration;
		this.snailMailSenderIntegration = snailMailSenderIntegration;
		this.slackIntegration = slackIntegration;
		this.oepIntegration = oepIntegration;
		this.messageMapper = messageMapper;
		this.requestMapper = requestMapper;
		this.dtoMapper = dtoMapper;
	}

	public InternalDeliveryResult sendSms(final SmsRequest request) {
		// Create batchId as history resource depends on it being instansiated
		final var batchId = UUID.randomUUID().toString();
		final var cleanedRequest = request.withSender(cleanSenderName(request.sender()));

		// Save the message and (try to) deliver it
		return deliver(dbIntegration.saveMessage(messageMapper.toMessage(cleanedRequest, batchId)));
	}

	public InternalDeliveryResult sendEmail(final EmailRequest request) {
		// Create batchId as history resource depends on it being instansiated
		final var batchId = UUID.randomUUID().toString();

		// Save the message and (try to) deliver it
		return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request, batchId)));
	}

	public InternalDeliveryResult sendWebMessage(final WebMessageRequest request) {
		// Create batchId as history resource depends on it being instansiated
		final var batchId = UUID.randomUUID().toString();

		// Save the message and (try to) deliver it
		return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request, batchId)));
	}

	public InternalDeliveryBatchResult sendDigitalMail(final DigitalMailRequest request, String organizationNumber) {
		final var batchId = UUID.randomUUID().toString();
		// Save the message(s)
		final var deliveries = dbIntegration.saveMessages(messageMapper.toMessages(request, batchId, organizationNumber));
		// Deliver them
		final var deliveryResults = deliveries.stream()
			.map(this::deliver)
			.toList();

		return new InternalDeliveryBatchResult(batchId, deliveryResults, request.municipalityId());
	}

	public InternalDeliveryResult sendDigitalInvoice(final DigitalInvoiceRequest request) {
		// Create batchId as history resource depends on it being instansiated
		final var batchId = UUID.randomUUID().toString();

		// Save the message and (try to) deliver it
		return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request, batchId)));
	}

	public InternalDeliveryBatchResult sendMessages(final MessageRequest request) {
		final var batchId = UUID.randomUUID().toString();
		final var messages = request.messages().stream()
			.map(message -> messageMapper.toMessage(request.municipalityId(), request.origin(), request.issuer(), batchId, message))
			.map(dbIntegration::saveMessage)
			.toList();

		// Handle and send each message individually, since we don't know if it will result in zero,
		// one or more actual deliveries
		final var deliveryResults = messages.stream()
			.map(this::sendMessage)
			.flatMap(Collection::stream)
			.toList();

		return new InternalDeliveryBatchResult(batchId, deliveryResults, request.municipalityId());
	}

	public void sendLetter(final Message message) {
		final var batchId = message.batchId();

		final var deliveryResults = routeAndSendLetter(message);

		// Trigger the batch if no messages are left unsent
		if (!dbIntegration.existsByBatchId(batchId)) {
			LOG.info("Triggering async batch {}", batchId);

			sendSnailMailBatch(deliveryResults, batchId, message.municipalityId());
		} else {
			LOG.info("Not triggering batch {} since there are unhandled messages", batchId);
		}
	}

	public InternalDeliveryBatchResult sendLetter(final LetterRequest request, final String organizationNumber) {
		final var batchId = UUID.randomUUID().toString();

		final var messagesWithPartyId = messageMapper.toMessages(request, batchId, organizationNumber);
		final var messagesWithAddress = messageMapper.mapAddressesToMessages(request, batchId);
		final var allMessages = Stream.concat(messagesWithPartyId.stream(), messagesWithAddress.stream()).toList();
		dbIntegration.saveMessages(allMessages);

		// Handle and send each message individually, since we don't know if it will result in zero,
		// one or more actual deliveries
		final var deliveryResults = allMessages.stream()
			.map(this::routeAndSendLetter)
			.flatMap(Collection::stream)
			.toList();

		LOG.info("Triggering sync batch {}", batchId);

		sendSnailMailBatch(deliveryResults, batchId, request.municipalityId());

		return new InternalDeliveryBatchResult(batchId, deliveryResults, request.municipalityId());
	}

	private void sendSnailMailBatch(final List<InternalDeliveryResult> deliveryResults, final String batchId, final String municipalityId) {
		final var snailMailDeliveryResults = deliveryResults.stream().filter(deliveryResult -> SNAIL_MAIL.equals(deliveryResult.messageType())).toList();

		if (snailMailDeliveryResults.isEmpty()) {
			LOG.info("Not triggering batch {} since it contains no snail-mail deliveries", batchId);
		} else if (snailMailDeliveryResults.stream().allMatch(deliveryResult -> FAILED.equals(deliveryResult.status()))) {
			LOG.warn("Not triggering batch {} since all deliveries within it failed", batchId);
		} else {
			snailMailDeliveryResults.forEach(deliveryResult -> LOG.info("Delivery {} was sent as snail-mail", deliveryResult.deliveryId()));

			// At least one delivery was sent as snail-mail - send the batch
			snailMailSenderIntegration.sendBatch(municipalityId, batchId);

			LOG.info("Batch {} sent successfully", batchId);
		}
	}

	public InternalDeliveryResult sendToSlack(final SlackRequest request) {
		// Create batchId as history resource depends on it being instansiated
		final var batchId = UUID.randomUUID().toString();

		// Save the message and (try to) deliver it
		return deliver(dbIntegration.saveMessage(messageMapper.toMessage(request, batchId)));
	}

	List<InternalDeliveryResult> sendMessage(final Message message) {
		final var deliveryResults = new ArrayList<InternalDeliveryResult>();

		final var partyId = message.partyId();

		final var request = fromJson(message.content(), MessageRequest.Message.class);
		// Make sure we have been able to recreate the original request
		if (isNull(request)) {
			LOG.warn("Unable to deliver MESSAGE since the original request can't be recreated");

			final var failedMessage = message.withStatus(FAILED);
			archiveMessage(failedMessage, "Unable to recreate original MESSAGE request");

			return List.of(new InternalDeliveryResult(failedMessage));
		}

		// Get the message filters
		final var filters = ofNullable(request.filters())
			.map(LinkedMultiValueMap::new)
			.orElseGet(LinkedMultiValueMap::new);

		// Get contact settings and maybe act upon them
		final var contactSettings = contactSettingsIntegration.getContactSettings(message.municipalityId(), partyId, filters);
		if (contactSettings.isEmpty()) {
			LOG.info("No contact settings found for {} with filters {}", partyId, filters);

			// No contact settings found - can't do anything more here
			archiveMessage(message.withStatus(NO_CONTACT_SETTINGS_FOUND));

			deliveryResults.add(new InternalDeliveryResult(message, NO_CONTACT_SETTINGS_FOUND));
		} else {
			for (final var contactSetting : contactSettings) {
				// Determine the contact method, if any
				final var actualContactMethod = ofNullable(contactSetting.contactMethod())
					.map(contactMethod -> {
						if (contactSetting.disabled()) {
							return NO_CONTACT;
						}

						return contactMethod;
					})
					.orElse(UNKNOWN);

				// Re-map the delivery to use the actual contact method and deliver it
				switch (actualContactMethod) {
					case EMAIL -> {
						final var deliveryId = UUID.randomUUID().toString();

						LOG.info("Handling incoming message {} as e-mail with delivery id {}", message.messageId(), deliveryId);

						final var delivery = message
							.withDeliveryId(deliveryId)
							.withType(EMAIL)
							.withContent(requestMapper.toEmailRequest(message, contactSetting.destination()));

						// Save the re-mapped delivery
						dbIntegration.saveMessage(delivery);
						// Delete the original delivery
						dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

						deliveryResults.add(deliver(delivery));
					}
					case SMS -> {
						final var deliveryId = UUID.randomUUID().toString();

						LOG.info("Handling incoming message {} as SMS with delivery id {}", message.messageId(), deliveryId);

						final var delivery = message
							.withDeliveryId(deliveryId)
							.withType(SMS)
							.withContent(requestMapper.toSmsRequest(message, contactSetting.destination()));

						// Save the re-mapped delivery
						dbIntegration.saveMessage(delivery);
						// Delete the original delivery
						dbIntegration.deleteMessageByDeliveryId(message.deliveryId());

						deliveryResults.add(deliver(delivery));
					}
					case NO_CONTACT -> {
						LOG.info("No contact wanted for {} ({}). No delivery will be attempted", partyId, contactSetting.contactMethod());

						archiveMessage(message.withStatus(NO_CONTACT_WANTED));

						deliveryResults.add(new InternalDeliveryResult(message, NO_CONTACT_WANTED));
					}
					default -> {
						LOG.warn("Unknown/missing contact method for message {} and delivery id {} - will not be delivered",
							message.messageId(), message.deliveryId());

						final var statusDetail = String.format(
							"Unknown/missing contact method for message %s and delivery id %s",
							message.messageId(), message.deliveryId());

						archiveMessage(message.withStatus(FAILED), statusDetail);

						deliveryResults.add(new InternalDeliveryResult(message, FAILED));
					}
				}
			}
		}

		return deliveryResults;
	}

	List<InternalDeliveryResult> routeAndSendLetter(final Message message) {
		final var result = new ArrayList<InternalDeliveryResult>();
		final var request = fromJson(message.content(), LetterRequest.class);

		// Make sure we have been able to recreate the original request
		if (isNull(request)) {
			LOG.warn("Unable to deliver {} since the original LETTER request can't be recreated", message.type());

			final var failedMessage = message.withStatus(FAILED);
			archiveMessage(failedMessage, "Unable to recreate original LETTER request");

			return List.of(new InternalDeliveryResult(failedMessage));
		}

		if (isNotBlank(message.partyId())) {
			// Re-map the request as a digital mail request
			final var digitalMailRequest = requestMapper.toDigitalMailRequest(request, message.partyId());
			final var digitalMailRequestAsJson = toJson(digitalMailRequest);

			// Don't make an attempt to deliver as digital mail if there aren't any attachments
			// intended for it
			if (!digitalMailRequest.attachments().isEmpty()) {
				// "Re-route" the message as digital mail
				final var reroutedMessage = dbIntegration.saveMessage(message
					.withType(DIGITAL_MAIL)
					.withContent(digitalMailRequestAsJson));

				try {
					// Deliver it
					result.add(deliver(reroutedMessage));

					if (SENT.equals(result.getFirst().status())) {
						return result;
					}
				} catch (final Exception e) {
					LOG.info("Unable to send LETTER as DIGITAL_MAIL");

					result.add(new InternalDeliveryResult(reroutedMessage.withStatus(FAILED)));
				}
			} else {
				// No attachments intended for digital mail
				LOG.info("No attachment(s) for DIGITAL_MAIL - switching over to snail-mail");
			}
		}
		// We're about to switch to snail-mail delivery - make sure that there exists some attachment(s) for that
		else if (request.attachments().stream().noneMatch(LetterRequest.Attachment::isIntendedForSnailMail)) {
			// "Route" the failed message back to a LETTER
			final var failedMessage = message.withType(LETTER).withStatus(FAILED);
			archiveMessage(failedMessage, "Only DIGITAL_MAIL delivery allowed and party id unset/address set");

			return List.of(new InternalDeliveryResult(failedMessage));
		}

		// Re-map the request as a snail-mail request
		var snailMailRequest = requestMapper.toSnailMailRequest(request, message.partyId(), message.address());
		var snailMailRequestAsJson = toJson(snailMailRequest);

		// Don't make an attempt to deliver as snail mail if there aren't any attachments
		// intended for it
		if (!snailMailRequest.attachments().isEmpty()) {
			var address = message.address();
			// Lookup destination address from the citizen API, if needed
			if (address == null && isNotBlank(message.partyId())) {
				try {
					address = citizenIntegration.getCitizenAddress(message.partyId(), message.municipalityId());

					snailMailRequest = snailMailRequest.withAddress(address);
					snailMailRequestAsJson = toJson(snailMailRequest);
				} catch (final Exception e) {
					// If something went wrong fetching the address, there's nothing more to do with this message but to bail out early
					LOG.info("Unable to get address from citizen");

					final var failedMessage = message.withStatus(FAILED);
					archiveMessage(failedMessage, e.getMessage());
					result.add(new InternalDeliveryResult(failedMessage));
					return result;
				}
			}

			// Re-route the original message as snail-mail - if the party id is set on the original message,
			// we've gotten here after attempting a digital mail delivery, so we need to treat the delivery
			// as a new one with a fresh delivery id
			var deliveryIdModified = false;
			var deliveryId = message.deliveryId();
			if (isNotBlank(message.partyId())) {
				deliveryId = UUID.randomUUID().toString();
				deliveryIdModified = true;
			}

			final var reroutedMessage = dbIntegration.saveMessage(message
				.withDeliveryId(deliveryId)
				.withType(SNAIL_MAIL)
				.withContent(snailMailRequestAsJson)
				.withAddress(address));

			try {
				// Deliver it
				result.add(deliver(reroutedMessage));
			} catch (final Exception e) {
				LOG.info("Unable to send LETTER as SNAIL_MAIL");

				result.add(new InternalDeliveryResult(reroutedMessage.withStatus(FAILED)));
			}

			// If we modified the delivery id we need to delete the original delivery manually
			if (deliveryIdModified) {
				dbIntegration.deleteMessageByDeliveryId(message.deliveryId());
			}
		} else {
			LOG.info("No attachment(s) for SNAIL_MAIL - unable to send letter");
		}

		return result;
	}

	InternalDeliveryResult deliver(final Message delivery) {
		// Re-construct the original request
		final var request = fromJson(delivery.content(), switch (delivery.type()) {
			case SMS -> SmsRequest.class;
			case EMAIL -> EmailRequest.class;
			case DIGITAL_MAIL -> DigitalMailRequest.class;
			case DIGITAL_INVOICE -> DigitalInvoiceRequest.class;
			case WEB_MESSAGE -> WebMessageRequest.class;
			case SNAIL_MAIL -> SnailMailRequest.class;
			case SLACK -> SlackRequest.class;
			default -> throw new IllegalArgumentException("Unknown request type: " + delivery.type());
		});

		// Get the delivery attempt for the given message type
		final Supplier<MessageStatus> deliveryAttempt = switch (delivery.type()) {
			case SMS -> () -> smsSenderIntegration.sendSms(delivery.municipalityId(), dtoMapper.toSmsDto((SmsRequest) request));
			case EMAIL -> () -> emailSenderIntegration.sendEmail(delivery.municipalityId(), dtoMapper.toEmailDto((EmailRequest) request));
			case DIGITAL_MAIL -> () -> digitalMailSenderIntegration.sendDigitalMail(delivery.municipalityId(), delivery.organizationNumber(), dtoMapper.toDigitalMailDto((DigitalMailRequest) request, delivery.partyId()));
			case DIGITAL_INVOICE -> () -> digitalMailSenderIntegration.sendDigitalInvoice(delivery.municipalityId(), dtoMapper.toDigitalInvoiceDto((DigitalInvoiceRequest) request));
			case WEB_MESSAGE -> () -> oepIntegration.sendWebMessage(delivery.municipalityId(), dtoMapper.toWebMessageDto((WebMessageRequest) request), ((WebMessageRequest) request).attachments());
			case SNAIL_MAIL -> () -> snailMailSenderIntegration.sendSnailMail(delivery.municipalityId(), dtoMapper.toSnailMailDto((SnailMailRequest) request, delivery.batchId(), delivery.address()));
			case SLACK -> () -> slackIntegration.sendMessage(dtoMapper.toSlackDto((SlackRequest) request));
			default -> throw new IllegalArgumentException("Unknown delivery type: " + delivery.type());
		};

		try {
			// Perform the attempt
			final var status = deliveryAttempt.get();
			// Archive the message
			archiveMessage(delivery.withStatus(status));

			return new InternalDeliveryResult(delivery.messageId(), delivery.deliveryId(), delivery.type(), status, delivery.municipalityId());
		} catch (final Exception e) {
			LOG.info("Unable to deliver {}: {}", delivery.type(), e.getMessage());

			// "Rewrite" the exception as a Problem if it isn't one already
			final ThrowableProblem throwableProblem;
			if (e instanceof final ThrowableProblem eAsThrowableProblem) {
				throwableProblem = eAsThrowableProblem;
			} else {
				throwableProblem = Problem.valueOf(Status.INTERNAL_SERVER_ERROR, "Unable to deliver " + delivery.type());
			}

			// Archive the message with FAILED status
			archiveMessage(delivery.withStatus(FAILED), e.getMessage());

			throw throwableProblem;
		}
	}

	void archiveMessage(final Message message) {
		archiveMessage(message, null);
	}

	void archiveMessage(final Message message, final String statusDetail) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {

			@Override
			protected void doInTransactionWithoutResult(@NotNull final TransactionStatus ignored) {
				LOG.info("Moving {} delivery {} with status {} to history", message.type(),
					message.deliveryId(), message.status());

				dbIntegration.saveHistory(message, statusDetail);
				dbIntegration.deleteMessageByDeliveryId(message.deliveryId());
			}
		});
	}

	public List<Mailbox> getMailboxes(final String municipalityId, final String organizationNumber, final List<String> partyIds) {
		return digitalMailSenderIntegration.getMailboxes(municipalityId, organizationNumber, partyIds);
	}
}

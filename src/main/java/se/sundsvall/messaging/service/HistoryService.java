package se.sundsvall.messaging.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import se.sundsvall.dept44.exception.ClientProblem;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.messaging.api.model.response.Batch;
import se.sundsvall.messaging.api.model.response.UserBatches;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.BatchHistoryProjection;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.integration.objectstore.ObjectStoreIntegration;
import se.sundsvall.messaging.integration.party.PartyIntegration;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.model.Attachment;
import se.sundsvall.messaging.util.FilterUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.toBatch;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.toStatus;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.toUserBatches;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.util.FilterUtils.isSnailMailSuccessful;
import static se.sundsvall.messaging.util.PagingUtil.toPage;

@Service
public class HistoryService {

	private static final Logger LOG = LoggerFactory.getLogger(HistoryService.class);

	private final DbIntegration dbIntegration;

	private final PartyIntegration partyIntegration;

	private final ObjectMapper objectMapper;

	private final BatchExtractor batchExtractor;

	private final ObjectStoreIntegration objectStoreIntegration;

	public HistoryService(final DbIntegration dbIntegration, final PartyIntegration partyIntegration, final ObjectMapper objectMapper, BatchExtractor batchDecorator,
		final ObjectStoreIntegration objectStoreIntegration) {
		this.dbIntegration = dbIntegration;
		this.partyIntegration = partyIntegration;
		this.objectMapper = objectMapper;
		this.batchExtractor = batchDecorator;
		this.objectStoreIntegration = objectStoreIntegration;
	}

	public List<History> getHistoryByMunicipalityIdAndMessageId(final String municipalityId, final String messageId) {
		return dbIntegration.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	public List<History> getHistoryByMunicipalityIdAndBatchId(final String municipalityId, final String batchId) {
		return dbIntegration.getHistoryByMunicipalityIdAndBatchId(municipalityId, batchId);
	}

	public Optional<History> getHistoryByMunicipalityIdAndDeliveryId(final String municipalityId, final String deliveryId) {
		return dbIntegration.getHistoryByMunicipalityIdAndDeliveryId(municipalityId, deliveryId);
	}

	public List<History> getConversationHistory(final String municipalityId, final String partyId, final LocalDate from,
		final LocalDate to) {
		return dbIntegration.getHistory(municipalityId, partyId, from, to);
	}

	public void streamAttachment(final String municipalityId, final String messageId, final String fileName, final HttpServletResponse response) throws IOException {
		final var history = dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		final var nameField = getFileNameField(history.getMessageType());
		final var content = objectMapper.readTree(history.getContent());

		final var attachment = findAttachmentByName(content, nameField, fileName);
		setupResponse(response, attachment);
	}

	Attachment findAttachmentByName(final JsonNode content, final String nameField, final String fileName) {
		final var attachments = content.get("attachments");
		if (attachments == null || !attachments.isArray()) {
			throw Problem.valueOf(NOT_FOUND, "Attachment with name " + fileName + " not found");
		}
		for (final var attachment : attachments) {
			// Every field is read through ofNullable rather than dereferenced. JsonNode.get returns null for an absent
			// field, and absent is ordinary here: contentType has always been optional and the mapper omits nulls, so
			// dereferencing it turned any mail sent without one into a 500 on this endpoint. An attachment stored as a
			// reference has no content node at all, for the same reason.
			final var name = text(attachment, nameField);
			if (fileName.equals(name)) {
				return Attachment.builder()
					.withName(name)
					.withContent(contentOf(attachment, fileName))
					.withContentType(text(attachment, "contentType"))
					.build();
			}
		}
		throw Problem.valueOf(NOT_FOUND, "Attachment with name " + fileName + " not found");
	}

	private static String text(final JsonNode node, final String field) {
		return ofNullable(field)
			.map(node::get)
			.map(JsonNode::asText)
			.orElse(null);
	}

	/**
	 * The attachment's bytes, base64-encoded, whether they were archived inline or left in the object store.
	 * <p>
	 * History keeps an attachment's metadata for as long as the message is kept, but a referenced attachment's bytes
	 * only live as long as the object store's time to live. That asymmetry is deliberate - the alternative puts the
	 * bytes back into the message row, which is the cost this whole arrangement exists to avoid - and it is why an
	 * expired object is answered with a plain 404 saying so rather than with a stack trace or a bad gateway.
	 */
	private String contentOf(final JsonNode attachment, final String fileName) {
		final var inlineContent = text(attachment, "content");
		if (StringUtils.isNotBlank(inlineContent)) {
			return inlineContent;
		}

		final var objectId = text(attachment, "objectId");
		if (StringUtils.isBlank(objectId)) {
			throw Problem.valueOf(NOT_FOUND, "Attachment with name " + fileName + " has no retained content");
		}

		try {
			return Base64.encodeBase64String(objectStoreIntegration.fetch(objectId).content());
		} catch (final ClientProblem e) {
			LOG.info("Attachment object {} is no longer available: {}", objectId, e.getMessage());
			throw Problem.valueOf(NOT_FOUND, "The content of attachment " + fileName + " is no longer retained");
		}
	}

	private void setupResponse(final HttpServletResponse response, final Attachment attachment) throws IOException {
		final var decodedContent = Base64.decodeBase64(attachment.getContent());
		response.addHeader(CONTENT_TYPE, attachment.getContentType());
		response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"");
		response.addHeader(CONTENT_LENGTH, String.valueOf(decodedContent.length));
		response.setContentLength(decodedContent.length);

		final var binaryStream = new ByteArrayInputStream(decodedContent);
		StreamUtils.copy(binaryStream, response.getOutputStream());
	}

	public UserBatches getUserBatches(final String municipalityId, final String issuer, final Integer page, final Integer limit) {
		final var thirtyDaysAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(30).atStartOfDay();
		final var batches = dbIntegration.getBatchHistoryMessagesForUser(municipalityId, issuer, thirtyDaysAgo).stream() // Fetch batchprojections for all messages sent the 30 last day for issuer
			.collect(groupingBy(BatchHistoryProjection::getBatchId)).entrySet().stream() // Group result by batch id and stream result (Map<batchId, List<BatchHistoryProjection>>)
			.map(entry -> createBatch(municipalityId, entry)) // To map each entry to a Batch object
			.filter(Objects::nonNull) // Just to be safe
			.sorted((o1, o2) -> ofNullable(o2.sent()).orElse(LocalDateTime.MAX).compareTo(ofNullable(o1.sent()).orElse(LocalDateTime.MAX))) // Sort on sent descending to have latest batches first in list
			.toList();

		return toUserBatches(toPage(page, limit, batches), page); // Return a paginated result with content matching requested page and limit
	}

	Batch createBatch(String municipalityId, Entry<String, List<BatchHistoryProjection>> batchHistoryProjectionEntry) {
		return ofNullable(batchHistoryProjectionEntry)
			.map(entry -> {
				final var batchId = entry.getKey();
				final var batchMessages = entry.getValue();

				final var sent = batchExtractor.extractSent(batchMessages);
				final var attachmentCount = batchExtractor.extractAttachmentCount(municipalityId, batchMessages);
				final var recipientCount = batchExtractor.extractRecipientCount(batchMessages);
				final var messageType = batchExtractor.extractOriginalMesageType(batchMessages);
				final var subject = batchExtractor.extractSubject(municipalityId, batchMessages);
				final var status = toStatus(
					batchExtractor.extractSuccessfulCount(batchMessages),
					batchExtractor.extractUnsuccessfulCount(batchMessages));

				return toBatch(batchId, sent, messageType, subject, attachmentCount, recipientCount, status);
			})
			.orElse(null);
	}

	public UserMessages getUserMessages(final String municipalityId, final String userId, String batchId, final Integer page, final Integer limit) {
		final var thirtyDaysAgo = LocalDateTime.now(ZoneId.systemDefault()).minusDays(30);
		final var pageRequest = PageRequest.of(page - 1, limit);
		final var messageIdPage = isNull(batchId) ? dbIntegration.getUniqueMessageIds(municipalityId, userId, thirtyDaysAgo, pageRequest)
			: dbIntegration.getUniqueMessageIds(municipalityId, batchId, userId, thirtyDaysAgo, pageRequest);

		return UserMessages.builder()
			.withMessages(createUserMessages(municipalityId, messageIdPage.getContent()))
			.withMetaData(PagingMetaData.create()
				.withPage(messageIdPage.getNumber() + 1)
				.withLimit(messageIdPage.getSize())
				.withCount(messageIdPage.getNumberOfElements())
				.withTotalRecords(messageIdPage.getTotalElements())
				.withTotalPages(messageIdPage.getTotalPages()))
			.build();
	}

	List<UserMessage> createUserMessages(final String municipalityId, final List<MessageIdProjection> messageIdProjections) {
		return messageIdProjections.stream()
			.map(MessageIdProjection::getMessageId)
			.map(messageId -> createUserMessage(municipalityId, messageId))
			.toList();
	}

	UserMessage createUserMessage(final String municipalityId, final String messageId) {
		final var histories = dbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		final var recipients = createRecipients(municipalityId, histories);
		final var history = histories.stream()
			.filter(h -> h.getMessageType() == MessageType.DIGITAL_MAIL)
			.findFirst().orElse(histories.getFirst());

		return UserMessage.builder()
			.withMessageId(messageId)
			.withIssuer(history.getIssuer())
			.withOrigin(history.getOrigin())
			.withSent(firstAttemptedAt(histories))
			.withRecipients(recipients)
			.withSubject(extractSubject(history))
			.withAttachments(extractAttachment(history))
			.withBody(extractMessage(history))
			.build();
	}

	/**
	 * The earliest row rather than whichever the query returned first: when a delivery was retried, the message was
	 * sent when the first attempt was made, not when the last one gave up.
	 */
	private static LocalDateTime firstAttemptedAt(final List<HistoryEntity> histories) {
		return histories.stream()
			.map(HistoryEntity::getCreatedAt)
			.filter(Objects::nonNull)
			.min(naturalOrder())
			.orElse(null);
	}

	private String extractMessage(final HistoryEntity history) {
		JsonNode content;
		try {
			content = objectMapper.readTree(history.getContent());
		} catch (final JacksonException ignored) {
			return "";
		}
		return ofNullable(content.get("message")).map(JsonNode::asText).orElse(null);
	}

	private String extractMobileNumber(final HistoryEntity history) {
		JsonNode content;
		try {
			content = objectMapper.readTree(history.getContent());
		} catch (final JacksonException ignored) {
			return null;
		}
		return ofNullable(content.get("mobileNumber")).map(JsonNode::asText).orElse(null);
	}

	String extractSubject(final HistoryEntity history) {
		JsonNode content;
		try {
			content = objectMapper.readTree(history.getContent());
		} catch (final JacksonException ignored) {
			return "";
		}
		return ofNullable(content.get("subject")).map(JsonNode::asText).orElse(null);
	}

	List<UserMessage.MessageAttachment> extractAttachment(final HistoryEntity history) {
		if (StringUtils.isBlank(history.getContent())) {
			return emptyList();
		}
		final List<UserMessage.MessageAttachment> attachments = new ArrayList<>();
		final var messageType = history.getMessageType();

		JsonNode attachmentsNode;

		try {
			final var jsonNode = objectMapper.readTree(history.getContent());
			final var attachmentsField = getAttachmentsField(messageType);
			attachmentsNode = jsonNode.get(attachmentsField);
		} catch (final JacksonException ignored) {
			return emptyList();
		}

		if (attachmentsNode != null && attachmentsNode.isArray()) {
			final var fileNameField = getFileNameField(messageType);
			final var contentTypeField = getContentTypeField(messageType);

			for (final var attachment : attachmentsNode) {
				attachments.add(UserMessage.MessageAttachment.builder()
					.withFileName(attachment.get(fileNameField).asText())
					.withContentType(attachment.get(contentTypeField).asText())
					.build());
			}
		}
		return attachments;
	}

	List<UserMessage.Recipient> createRecipients(final String municipalityId, final List<HistoryEntity> histories) {
		final var recipients = collapseDeliveryAttempts(histories).stream()
			.map(history -> createRecipient(municipalityId, history))
			.collect(toCollection(ArrayList::new));

		// Remove entries with messagetype DIGITAL_MAIL and status not equal to SENT if there exists an entry with same
		// personId with messagetype SNAIL_MAIL and status SENT
		recipients.removeAll(
			recipients.stream()
				.filter(FilterUtils::isDigitalMailAndUnsuccessful)
				.filter(recipient -> isSnailMailSuccessful(recipient.personId(), recipients))
				.toList());

		return recipients;
	}

	/**
	 * Reduces repeated delivery attempts at one recipient to the single attempt worth reporting.
	 * <p>
	 * A queued SMS that failed and was retried leaves one history row per attempt, all sharing a message id. Reporting
	 * each of them would show one SMS as having been sent to four people. A successful attempt is what happened;
	 * failing that, the last attempt is.
	 * <p>
	 * Only the channels that have a retry ladder are considered - SMS and e-mail - since only those can produce several
	 * rows that are attempts at one delivery rather than separate deliveries. Two identical snail mails under one
	 * message id are two real deliveries and are left alone. Within a channel the destination is the discriminator,
	 * since a MESSAGE request fans out to one delivery per contact setting under a single message id, and those are
	 * genuinely different recipients.
	 */
	List<HistoryEntity> collapseDeliveryAttempts(final List<HistoryEntity> histories) {
		final var reported = Collections.newSetFromMap(new IdentityHashMap<HistoryEntity, Boolean>());

		histories.stream()
			.filter(HistoryService::hasRetryLadder)
			.collect(groupingBy(this::recipientKey))
			.values()
			.forEach(attempts -> reported.add(attemptToReport(attempts)));

		// Filtering the original list rather than rebuilding it keeps the recipients in the order they arrived.
		return histories.stream()
			.filter(history -> !hasRetryLadder(history) || reported.contains(history))
			.toList();
	}

	private static HistoryEntity attemptToReport(final List<HistoryEntity> attempts) {
		return attempts.stream()
			.filter(attempt -> attempt.getStatus() == SENT)
			.findFirst()
			.orElseGet(() -> attempts.stream()
				.max(comparing(HistoryEntity::getCreatedAt, nullsFirst(naturalOrder())))
				.orElseThrow());
	}

	private static boolean hasRetryLadder(final HistoryEntity history) {
		return history.getMessageType() == SMS || history.getMessageType() == EMAIL;
	}

	private List<Object> recipientKey(final HistoryEntity history) {
		// Nulls are expected - neither channel need carry a party id - so the key is an Arrays.asList rather than a
		// record, which keeps null handling out of it. The message type is part of the key so that a party reached on
		// both channels under one message id is not collapsed into one recipient.
		return Arrays.asList(history.getMessageType(), history.getPartyId(), extractDestination(history));
	}

	/**
	 * The address an attempt was made against: a mobile number for SMS, a recipient address for e-mail. It is read out
	 * of the stored request rather than off the row, because that is where the value a retry shares with its earlier
	 * attempts lives.
	 */
	private String extractDestination(final HistoryEntity history) {
		if (history.getMessageType() == EMAIL) {
			return extractEmailAddress(history);
		}
		return extractMobileNumber(history);
	}

	private String extractEmailAddress(final HistoryEntity history) {
		JsonNode content;
		try {
			content = objectMapper.readTree(history.getContent());
		} catch (final JacksonException ignored) {
			return null;
		}
		// A queued e-mail carries a single address on emailAddress; one submitted over HTTP may instead carry a
		// recipients list, and its first entry is what a retry would repeat.
		return ofNullable(content.get("emailAddress"))
			.map(JsonNode::asText)
			.orElseGet(() -> ofNullable(content.get("recipients"))
				.filter(JsonNode::isArray)
				.filter(recipients -> !recipients.isEmpty())
				.map(recipients -> recipients.get(0).asText())
				.orElse(null));
	}

	UserMessage.Recipient createRecipient(final String municipalityId, final HistoryEntity history) {
		final var legalId = ofNullable(history.getPartyId())
			.map(party -> partyIntegration.getLegalIdByPartyId(municipalityId, party))
			.orElse(null);

		return UserMessage.Recipient.builder()
			.withStatus(history.getStatus().name())
			.withMessageType(history.getMessageType().toString())
			.withAddress(createAddress(history.getDestinationAddress()))
			.withMobileNumber(extractMobileNumber(history))
			.withPersonId(legalId)
			.build();
	}

	UserMessage.Address createAddress(final Address address) {
		return ofNullable(address)
			.map(addr -> UserMessage.Address.builder()
				.withAddress(addr.address())
				.withCity(addr.city())
				.withCountry(addr.country())
				.withFirstName(addr.firstName())
				.withLastName(addr.lastName())
				.withOrganizationName(addr.organizationName())
				.withCareOf(addr.careOf())
				.withZipCode(addr.zipCode())
				.build())
			.orElse(null);
	}

	/**
	 * Retrieves a specific user message based on the municipalityId, issuer, and messageId.
	 *
	 * @param  municipalityId The municipality ID.
	 * @param  issuer         The issuer of the message.
	 * @param  messageId      The message ID.
	 * @return                The UserMessage.
	 */
	public UserMessage getUserMessage(final String municipalityId, final String issuer, final String messageId) {
		// Do a sanity check that the messageId for the user exists, the "createUserMessage" doesn't check this
		if (!dbIntegration.existsByMunicipalityIdAndMessageIdAndIssuer(municipalityId, messageId, issuer)) {
			throw Problem.valueOf(NOT_FOUND, "No message found for message id " + messageId + " and user id " + issuer);
		}

		return createUserMessage(municipalityId, messageId);
	}

	/**
	 * Get the attachments field name for the given message type.
	 *
	 * @param  messageType messageType to get the attachments field for
	 * @return             the attachments field name
	 */
	String getAttachmentsField(final MessageType messageType) {
		return switch (messageType) {
			case DIGITAL_MAIL, EMAIL, LETTER, SNAIL_MAIL, WEB_MESSAGE -> "attachments";
			case DIGITAL_INVOICE -> "files";
			default -> null; // SMS, MESSAGE and SLACK
		};
	}

	/**
	 * Get the name field name for the given message type.
	 *
	 * @param  messageType messageType to get the name field for
	 * @return             the name field name
	 */
	String getFileNameField(final MessageType messageType) {
		return switch (messageType) {
			case DIGITAL_INVOICE, DIGITAL_MAIL, LETTER, SNAIL_MAIL -> "filename";
			case EMAIL -> "name";
			case WEB_MESSAGE -> "fileName";
			default -> null; // SMS, MESSAGE and SLACK
		};
	}

	/**
	 * Get the content type field name for the given message type.
	 *
	 * @param  messageType messageType to get the content type field name for
	 * @return             the content type field name
	 */
	String getContentTypeField(final MessageType messageType) {
		return switch (messageType) {
			case DIGITAL_INVOICE, DIGITAL_MAIL, EMAIL, LETTER, SNAIL_MAIL -> "contentType";
			case WEB_MESSAGE -> "mimeType";
			default -> null; // SMS, MESSAGE and SLACK
		};
	}
}

package se.sundsvall.messaging.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_LENGTH;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.zalando.problem.Status.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.utils.Base64;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.integration.party.PartyIntegration;
import se.sundsvall.messaging.model.Address;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.model.Attachment;

@Service
public class HistoryService {

	private final DbIntegration dbIntegration;

	private final PartyIntegration partyIntegration;

	private final ObjectMapper objectMapper;

	public HistoryService(final DbIntegration dbIntegration, final PartyIntegration partyIntegration, final ObjectMapper objectMapper) {
		this.dbIntegration = dbIntegration;
		this.partyIntegration = partyIntegration;
		this.objectMapper = objectMapper;
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
			final var name = attachment.get(nameField).asText();
			if (fileName.equals(name)) {
				return Attachment.builder()
					.withName(name)
					.withContent(attachment.get("content").asText())
					.withContentType(attachment.get("contentType").asText())
					.build();
			}
		}
		throw Problem.valueOf(NOT_FOUND, "Attachment with name " + fileName + " not found");
	}

	private void setupResponse(final HttpServletResponse response, final Attachment attachment) throws IOException {
		final var decodedContent = Base64.decodeBase64(attachment.getContent());
		response.addHeader(CONTENT_TYPE, attachment.getContentType());
		response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"");
		response.addHeader(CONTENT_LENGTH, String.valueOf(decodedContent.length));
		response.setContentLength(decodedContent.length);

		final var binaryStream = new BinaryStreamImpl(decodedContent);
		StreamUtils.copy(binaryStream, response.getOutputStream());
	}

	public UserMessages getUserMessages(final String municipalityId, final String userId, String batchId, final Integer page, final Integer limit) {
		final var thirtyDaysAgo = LocalDateTime.now().minusDays(30);
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

	List<UserMessage> createUserMessages(final String municipalityId, final List<MessageIdProjection> messageIds) {
		return messageIds.stream()
			.map(MessageIdProjection::getMessageId)
			.map(messageId -> createUserMessage(municipalityId, messageId))
			.toList();
	}

	UserMessage createUserMessage(final String municipalityId, final String messageId) {
		final var histories = dbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		final var recipients = createRecipients(municipalityId, histories);
		final var history = histories.stream()
			.filter(history1 -> history1.getMessageType() == MessageType.DIGITAL_MAIL)
			.findFirst().orElse(histories.getFirst());
		final var attachments = extractAttachment(history);
		final var subject = extractSubject(history);

		return UserMessage.builder()
			.withMessageId(messageId)
			.withIssuer(history.getIssuer())
			.withOrigin(history.getOrigin())
			.withSent(history.getCreatedAt())
			.withSubject(subject)
			.withRecipients(recipients)
			.withAttachments(attachments)
			.build();
	}

	String extractSubject(final HistoryEntity history) {
		JsonNode content;
		try {
			content = objectMapper.readTree(history.getContent());
		} catch (final JsonProcessingException ignored) {
			return "";
		}
		return Optional.ofNullable(content.get("subject")).map(JsonNode::asText).orElse("");
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
		} catch (final JsonProcessingException ignored) {
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
		return histories.stream().map(history -> {
			final var legalId = Optional.ofNullable(history.getPartyId())
				.map(party -> partyIntegration.getLegalIdByPartyId(municipalityId, party))
				.orElse(null);
			final var messageType = history.getMessageType().toString();
			final var status = history.getStatus().name();
			final var address = history.getDestinationAddress();
			return new UserMessage.Recipient(createAddress(address), legalId, messageType, status);
		}).toList();
	}

	UserMessage.Address createAddress(Address address) {
		return Optional.ofNullable(address)
			.map(addr -> UserMessage.Address.builder()
				.withAddress(addr.address())
				.withCity(addr.city())
				.withCountry(addr.country())
				.withFirstName(addr.firstName())
				.withLastName(addr.lastName())
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

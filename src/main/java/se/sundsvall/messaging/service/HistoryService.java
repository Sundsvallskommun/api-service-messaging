package se.sundsvall.messaging.service;

import static java.util.Collections.emptyList;
import static org.apache.hc.core5.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.zalando.problem.Status.NOT_FOUND;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
		var history = dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		var nameField = getNameField(history.getMessageType());
		var content = objectMapper.readTree(history.getContent());

		var attachment = findAttachmentByName(content, nameField, fileName);
		setupResponse(response, attachment);
	}

	public UserMessages getUserMessages(final String municipalityId, final String userId, final Integer page, final Integer limit) {
		var messageIdPage = dbIntegration.getUniqueMessageIds(municipalityId, userId, PageRequest.of(page - 1, limit));

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

	private String getNameField(final MessageType messageType) {
		return messageType.equals(MessageType.SNAIL_MAIL) ? "name" : "filename";
	}

	Attachment findAttachmentByName(final JsonNode content, final String nameField, final String fileName) {
		var attachments = content.get("attachments");
		if (attachments == null || !attachments.isArray()) {
			throw Problem.valueOf(NOT_FOUND, "Attachment with name " + fileName + " not found");
		}
		for (var attachment : attachments) {
			var name = attachment.get(nameField).asText();
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
		response.addHeader(CONTENT_TYPE, attachment.getContentType());
		response.addHeader(CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getName() + "\"");

		var binaryStream = new BinaryStreamImpl(Base64.decodeBase64(attachment.getContent()));
		StreamUtils.copy(binaryStream, response.getOutputStream());
	}

	List<UserMessage> createUserMessages(final String municipalityId, final List<MessageIdProjection> messageIds) {
		return messageIds.stream()
			.map(MessageIdProjection::getMessageId)
			.map(messageId -> createUserMessage(municipalityId, messageId))
			.toList();
	}

	UserMessage createUserMessage(final String municipalityId, final String messageId) {
		var histories = dbIntegration.getHistoryEntityByMunicipalityIdAndMessageId(municipalityId, messageId);
		var recipients = createRecipients(municipalityId, histories);
		var history = histories.getFirst();
		var attachments = extractAttachment(history);

		return UserMessage.builder()
			.withMessageId(messageId)
			.withIssuer(history.getIssuer())
			.withOrigin(history.getOrigin())
			.withSent(history.getCreatedAt())
			.withRecipients(recipients)
			.withAttachments(attachments)
			.build();
	}

	List<UserMessage.MessageAttachment> extractAttachment(final HistoryEntity history) {
		List<UserMessage.MessageAttachment> attachments = new ArrayList<>();
		var fieldName = getNameField(history.getMessageType());
		JsonNode attachmentsNode;
		try {
			var jsonNode = objectMapper.readTree(history.getContent());
			attachmentsNode = jsonNode.get("attachments");
		} catch (JsonProcessingException ignored) {
			return emptyList();
		}

		if (attachmentsNode != null && attachmentsNode.isArray()) {
			for (var attachment : attachmentsNode) {
				attachments.add(UserMessage.MessageAttachment.builder()
					.withFileName(attachment.get(fieldName).asText())
					.withContentType(attachment.get("contentType").asText())
					.build());
			}
		}
		return attachments;
	}

	List<UserMessage.Recipient> createRecipients(final String municipalityId, final List<HistoryEntity> histories) {
		return histories.stream().map(history -> {
			var legalId = partyIntegration.getLegalIdByPartyId(municipalityId, history.getPartyId());
			var messageType = history.getMessageType().toString();
			var status = history.getStatus().name();
			return new UserMessage.Recipient(legalId, messageType, status);
		}).toList();
	}

}

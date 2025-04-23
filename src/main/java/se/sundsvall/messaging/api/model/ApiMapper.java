package se.sundsvall.messaging.api.model;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;

public class ApiMapper {

	private static final Logger LOG = LoggerFactory.getLogger(ApiMapper.class);

	private static final List<String> ATTACHMENT_FIELDS = List.of("attachments", "files");
	private static final List<String> ATTACHMENT_CONTENT_FIELDS = List.of("content", "base64Data");

	private ApiMapper() {}

	public static ResponseEntity<MessageResult> toResponse(final InternalDeliveryResult deliveryResult) {
		final var uri = fromPath(MESSAGE_STATUS_PATH)
			.buildAndExpand(deliveryResult.municipalityId(), deliveryResult.messageId())
			.toUri();

		return created(uri)
			.body(MessageResult.builder()
				.withMessageId(deliveryResult.messageId())
				.withDeliveries(List.of(DeliveryResult.builder()
					.withDeliveryId(deliveryResult.deliveryId())
					.withMessageType(deliveryResult.messageType())
					.withStatus(deliveryResult.status())
					.build()))
				.build());
	}

	public static ResponseEntity<MessageBatchResult> toResponse(final InternalDeliveryBatchResult deliveryBatchResult) {
		final var uri = fromPath(BATCH_STATUS_PATH)
			.buildAndExpand(deliveryBatchResult.municipalityId(), deliveryBatchResult.batchId())
			.toUri();

		// Group the deliveries by message id
		final var groupedDeliveries = deliveryBatchResult.deliveries().stream()
			.collect(groupingBy(InternalDeliveryResult::messageId));

		return created(uri)
			.body(MessageBatchResult.builder()
				.withBatchId(deliveryBatchResult.batchId())
				.withMessages(groupedDeliveries.entrySet().stream()
					.map(message -> MessageResult.builder()
						.withMessageId(message.getKey())
						.withDeliveries(message.getValue().stream()
							.map(delivery -> DeliveryResult.builder()
								.withDeliveryId(delivery.deliveryId())
								.withMessageType(delivery.messageType())
								.withStatus(delivery.status())
								.build())
							.toList())
						.build())
					.toList())
				.build());
	}

	public static DeliveryResult toDeliveryResult(final History deliveryHistory) {
		return DeliveryResult.builder()
			.withDeliveryId(deliveryHistory.deliveryId())
			.withMessageType(deliveryHistory.messageType())
			.withStatus(deliveryHistory.status())
			.build();
	}

	public static HistoryResponse toHistoryResponse(final History history) {
		return HistoryResponse.builder()
			.withMessageType(history.messageType())
			.withStatus(history.status())
			.withContent(fromJson(history.content(), getType(history)))
			.withTimestamp(history.createdAt())
			.build();
	}

	/**
	 * Creates a HistoryResponse from a History object, but removes the file content from the attachment.
	 * 
	 * @param  history the history to convert
	 * @return         the HistoryResponse without attachments
	 */
	public static HistoryResponse toMetadataHistoryResponse(final History history) {
		return HistoryResponse.builder()
			.withMessageType(history.messageType())
			.withStatus(history.status())
			.withTimestamp(history.createdAt())
			.withContent(fromJson(removeAttachmentsFromHistory(history.content()), getType(history)))
			.build();
	}

	private static Type getType(History history) {
		return switch (history.messageType()) {
			case EMAIL -> EmailRequest.class;
			case SMS -> SmsRequest.class;
			case WEB_MESSAGE -> WebMessageRequest.class;
			case DIGITAL_MAIL -> DigitalMailRequest.class;
			case DIGITAL_INVOICE -> DigitalInvoiceRequest.class;
			case MESSAGE -> MessageRequest.Message.class;
			case SNAIL_MAIL -> SnailMailRequest.class;
			case LETTER -> LetterRequest.class;
			case SLACK -> SlackRequest.class;
		};
	}

	/**
	 * Removes the file content from the attachments.
	 * Base64 content is not stored in the same manner for all requests
	 * so we need to check for both "content" and"base64Data" fields (defined in ATTACHMENT_CONTENT_FIELDS).
	 * If we cannot parse the content, we return the original content.
	 * 
	 * @param  content the attachments from the history to remove file content from
	 * @return         the attachments without file content
	 */
	private static String removeAttachmentsFromHistory(String content) {

		if (StringUtils.isNotBlank(content)) {
			try {
				var objectMapper = new ObjectMapper();
				var jsonNode = objectMapper.readTree(content);
				removeAttachmentFileContents(jsonNode);
				content = objectMapper.writeValueAsString(jsonNode);
			} catch (JsonProcessingException e) {
				// We couldn't parse the content, do nothing
				LOG.warn("Couldn't remove attachment content from history, no big issue", e);
			}
		}

		// If we couldn't parse the content, just return the original content
		return content;
	}

	/**
	 * Remove the "content" and "base64Data" fields from the attachments
	 * 
	 * @param root the "node" to process
	 */
	private static void removeAttachmentFileContents(JsonNode root) {
		ATTACHMENT_FIELDS.stream()
			.filter(root::has)  // Check that the field exists
			.map(root::get)// Get the field
			.filter(Objects::nonNull)
			.filter(JsonNode::isArray) // Check that the field is an array
			.forEach(attachments -> attachments.forEach(attachment -> {
				// Make sure it's an object node otherwise we cannot remove the fields
				if (attachment instanceof ObjectNode objectNode) {
					// Remove the content field from the attachment
					ATTACHMENT_CONTENT_FIELDS.forEach(objectNode::remove);
				}
			}));
	}

	public static MessageBatchResult toMessageBatchResult(final List<History> history) {
		// Group the history first by batchId and then by messageId
		final var groupedHistory = history.stream()
			.collect(groupingBy(History::batchId, groupingBy(History::messageId)));

		// Sanity check - we should only have a single "root" entry, but just to be safe...
		if (groupedHistory.size() != 1) {
			throw Problem.valueOf(Status.NOT_FOUND, "Unable to get batch status");
		}

		// Grab the first (and only) "root" entry
		final var batch = groupedHistory
			.entrySet()
			.iterator()
			.next();

		return MessageBatchResult.builder()
			.withBatchId(batch.getKey())
			.withMessages(batch.getValue().entrySet().stream()
				.map(message -> MessageResult.builder()
					.withMessageId(message.getKey())
					.withDeliveries(message.getValue().stream()
						.map(ApiMapper::toDeliveryResult)
						.toList())
					.build())
				.toList())
			.build();
	}

	public static MessageResult toMessageResult(final List<History> history) {
		// Group the history by messageId
		final var groupedHistory = history.stream().collect(groupingBy(History::messageId));

		// Sanity check - we should only have a single "root" entry, but just to be safe...
		if (groupedHistory.size() != 1) {
			throw Problem.valueOf(Status.NOT_FOUND, "Unable to get message status");
		}

		// Grab the first (and only) "root" entry
		final var message = groupedHistory
			.entrySet()
			.iterator()
			.next();

		return MessageResult.builder()
			.withMessageId(message.getKey())
			.withDeliveries(message.getValue().stream()
				.map(ApiMapper::toDeliveryResult)
				.toList())
			.build();
	}

}

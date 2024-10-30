package se.sundsvall.messaging.api.model;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;

import java.util.List;

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
			.withContent(fromJson(history.content(), switch (history.messageType())
			{
				case EMAIL -> EmailRequest.class;
				case SMS -> SmsRequest.class;
				case WEB_MESSAGE -> WebMessageRequest.class;
				case DIGITAL_MAIL -> DigitalMailRequest.class;
				case DIGITAL_INVOICE -> DigitalInvoiceRequest.class;
				case MESSAGE -> MessageRequest.Message.class;
				case SNAIL_MAIL -> SnailMailRequest.class;
				case LETTER -> LetterRequest.class;
				case SLACK -> SlackRequest.class;
			}))
			.withTimestamp(history.createdAt())
			.build();
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

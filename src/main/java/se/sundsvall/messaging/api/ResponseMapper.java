package se.sundsvall.messaging.api;

import static java.util.stream.Collectors.groupingBy;
import static org.springframework.http.ResponseEntity.created;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.MESSAGE_STATUS_PATH;

import java.net.URI;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
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

final class ResponseMapper {

    private static final Gson GSON = new GsonBuilder().create();

    private ResponseMapper() { }

    static DeliveryResult toDeliveryResult(final History deliveryHistory) {
        return DeliveryResult.builder()
            .withDeliveryId(deliveryHistory.deliveryId())
            .withMessageType(deliveryHistory.messageType())
            .withStatus(deliveryHistory.status())
            .build();
    }

    static HistoryResponse toHistoryResponse(final History history) {
        return HistoryResponse.builder()
            .withMessageType(history.messageType())
            .withStatus(history.status())
            .withContent(GSON.fromJson(history.content(), switch (history.messageType()) {
                case EMAIL -> EmailRequest.class;
                case SMS -> SmsRequest.class;
                case WEB_MESSAGE -> WebMessageRequest.class;
                case DIGITAL_MAIL -> DigitalMailRequest.class;
                case MESSAGE -> MessageRequest.Message.class;
                case SNAIL_MAIL -> SnailMailRequest.class;
                case LETTER -> LetterRequest.class;
            }))
            .withTimestamp(history.createdAt())
            .build();
    }

    static ResponseEntity<MessageResult> toResponse(final InternalDeliveryResult deliveryResult) {
        return created(createMessageStatusUri(deliveryResult.messageId()))
            .body(MessageResult.builder()
                .withMessageId(deliveryResult.messageId())
                .withDeliveries(List.of(DeliveryResult.builder()
                    .withDeliveryId(deliveryResult.deliveryId())
                    .withMessageType(deliveryResult.messageType())
                    .withStatus(deliveryResult.status())
                    .build()))
                .build());
    }

    static ResponseEntity<MessageBatchResult> toResponse(final InternalDeliveryBatchResult deliveryBatchResult) {
        // Group the deliveries by message id
        var groupedDeliveries = deliveryBatchResult.deliveries().stream()
            .collect(groupingBy(InternalDeliveryResult::messageId));

        return created(createBatchStatusUri(deliveryBatchResult.batchId()))
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

    static URI createMessageStatusUri(final String messageId) {
        return UriComponentsBuilder.newInstance()
            .path(MESSAGE_STATUS_PATH)
            .buildAndExpand(messageId)
            .toUri();
    }

    static URI createBatchStatusUri(final String batchId) {
        return UriComponentsBuilder.newInstance()
            .path(BATCH_STATUS_PATH)
            .buildAndExpand(batchId)
            .toUri();
    }
}

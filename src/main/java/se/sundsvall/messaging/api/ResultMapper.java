package se.sundsvall.messaging.api;

import static org.springframework.http.ResponseEntity.created;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;

final class ResultMapper {

    private ResultMapper() { }

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
        return created(createBatchStatusUri(deliveryBatchResult.batchId()))
            .body(MessageBatchResult.builder()
                .withBatchId(deliveryBatchResult.batchId())
                .withMessages(deliveryBatchResult.deliveries().stream()
                    .map(message -> MessageResult.builder()
                        .withMessageId(message.messageId())
                        .withDeliveries(deliveryBatchResult.deliveries().stream()
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
            .path(StatusAndHistoryResource.MESSAGE_STATUS_PATH)
            .buildAndExpand(messageId)
            .toUri();
    }

    static URI createBatchStatusUri(final String batchId) {
        return UriComponentsBuilder.newInstance()
            .path(StatusAndHistoryResource.BATCH_STATUS_PATH)
            .buildAndExpand(batchId)
            .toUri();
    }
}

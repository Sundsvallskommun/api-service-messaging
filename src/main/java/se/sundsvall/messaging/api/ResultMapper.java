package se.sundsvall.messaging.api;

import static org.springframework.http.ResponseEntity.created;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.DeliveryBatchResult;
import se.sundsvall.messaging.model.DeliveryResult;

public final class ResultMapper {

    private ResultMapper() { }

    static ResponseEntity<MessageResult> toResponse(final DeliveryResult deliveryResult) {
        return created(createMessageStatusUri(deliveryResult.messageId()))
            .body(MessageResult.builder()
                .withMessageId(deliveryResult.messageId())
                .withDeliveryId(deliveryResult.deliveryId())
                .withStatus(deliveryResult.status())
                .build());
    }

    static ResponseEntity<MessageBatchResult> toResponse(final DeliveryBatchResult deliveryBatchResult) {
        return created(createBatchStatusUri(deliveryBatchResult.batchId()))
            .body(MessageBatchResult.builder()
                .withBatchId(deliveryBatchResult.batchId())
                .withMessages(deliveryBatchResult.deliveries().stream()
                    .map(delivery -> MessageResult.builder()
                        .withMessageId(delivery.messageId())
                        .withDeliveryId(delivery.deliveryId())
                        .withStatus(delivery.status())
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

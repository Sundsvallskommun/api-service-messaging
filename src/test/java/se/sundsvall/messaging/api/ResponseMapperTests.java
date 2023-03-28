package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.api.StatusAndHistoryResource.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.MessageType;

class ResponseMapperTests {

    @Test
    void test_toDeliveryResult() {
        var history = History.builder()
            .withDeliveryId("someDeliveryId")
            .withMessageType(WEB_MESSAGE)
            .withStatus(FAILED)
            .build();

        var result = ResponseMapper.toDeliveryResult(history);

        assertThat(result.deliveryId()).isEqualTo(history.deliveryId());
        assertThat(result.messageType()).isEqualTo(history.messageType());
        assertThat(result.status()).isEqualTo(history.status());
    }

    @ParameterizedTest
    @EnumSource(MessageType.class)
    void test_toHistoryResponse(final MessageType messageType) {
        var history = History.builder()
            .withMessageType(messageType)
            .withStatus(SENT)
            .withContent("{}")
            .withCreatedAt(LocalDateTime.now())
            .build();

        var result = ResponseMapper.toHistoryResponse(history);

        assertThat(result).isNotNull();
        assertThat(result.messageType()).isEqualTo(messageType);
        assertThat(result.content()).isNotNull();
        assertThat(result.status()).isEqualTo(SENT);
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    void test_toResponse_fromDeliveryResult() {
        var deliveryResult = InternalDeliveryResult.builder()
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withMessageType(DIGITAL_MAIL)
            .withStatus(SENT)
            .build();

        var result = ResponseMapper.toResponse(deliveryResult);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(CREATED);
        assertThat(result.getBody()).isNotNull().satisfies(message -> {
            assertThat(message.messageId()).isEqualTo(deliveryResult.messageId());
            assertThat(message.deliveries()).hasSize(1).allSatisfy(delivery -> {
                assertThat(delivery.deliveryId()).isEqualTo(deliveryResult.deliveryId());
                assertThat(delivery.messageType()).isEqualTo(deliveryResult.messageType());
                assertThat(delivery.status()).isEqualTo(deliveryResult.status());
            });
        });
    }

    @Test
    void test_toResponse_fromBatchResult() {
        var deliveryBatchResult = InternalDeliveryBatchResult.builder()
            .withBatchId("someBatchId")
            .withDeliveries(List.of(
                InternalDeliveryResult.builder()
                    .withMessageId("someMessageId")
                    .withDeliveryId("someDeliveryId")
                    .withMessageType(DIGITAL_MAIL)
                    .withStatus(FAILED)
                    .build(),
                InternalDeliveryResult.builder()
                    .withMessageId("someMessageId")
                    .withDeliveryId("someOtherDeliveryId")
                    .withMessageType(SNAIL_MAIL)
                    .withStatus(SENT)
                    .build()
            ))
            .build();

        var result = ResponseMapper.toResponse(deliveryBatchResult);

        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(CREATED);
        assertThat(result.getBody()).isNotNull().satisfies(batch -> {
            assertThat(batch.batchId()).isEqualTo(deliveryBatchResult.batchId());
            assertThat(batch.messages()).hasSize(1).allSatisfy(message -> {
                assertThat(message.messageId()).isNotNull();
                assertThat(message.deliveries()).hasSize(2);
                assertThat(message.deliveries()).extracting(DeliveryResult::deliveryId).doesNotContainNull();
                assertThat(message.deliveries()).extracting(DeliveryResult::messageType)
                    .containsExactlyInAnyOrder(DIGITAL_MAIL, SNAIL_MAIL);
                assertThat(message.deliveries()).extracting(DeliveryResult::status)
                    .containsExactlyInAnyOrder(FAILED, SENT);
            });
        });
    }

    @Test
    void test_createMessageStatusUri() {
        var messageId = "someMessageId";

        var result = ResponseMapper.createMessageStatusUri(messageId);

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(MESSAGE_STATUS_PATH.replace("{messageId}", messageId));
    }

    @Test
    void test_createBatchStatusUri() {
        var batchId = "someBatchId";

        var result = ResponseMapper.createBatchStatusUri(batchId);

        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(BATCH_STATUS_PATH.replace("{batchId}", batchId));
    }
}

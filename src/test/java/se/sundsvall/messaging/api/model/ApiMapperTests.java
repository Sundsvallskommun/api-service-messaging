package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class ApiMapperTests {

	private final InternalDeliveryResult deliveryResult = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withStatus(SENT)
		.build();

	@Test
	void toResponse_fromDeliveryResult() {
		// Act
		final var result = ApiMapper.toResponse(deliveryResult);

		// Assert and verify
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
	void toResponse_fromBatchResult() {
		// Arrange
		final var deliveryBatchResult = InternalDeliveryBatchResult.builder()
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
					.build()))
			.build();

		// Act
		final var result = ApiMapper.toResponse(deliveryBatchResult);

		// Assert and verify
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
	void toDeliveryResult() {
		// Arrange
		final var history = History.builder()
			.withDeliveryId("someDeliveryId")
			.withMessageType(WEB_MESSAGE)
			.withStatus(FAILED)
			.build();

		// Act
		final var result = ApiMapper.toDeliveryResult(history);

		// Assert and verify
		assertThat(result.deliveryId()).isEqualTo(history.deliveryId());
		assertThat(result.messageType()).isEqualTo(history.messageType());
		assertThat(result.status()).isEqualTo(history.status());
	}

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void toHistoryResponse(final MessageType messageType) {
		// Arrange
		final var history = History.builder()
			.withMessageType(messageType)
			.withStatus(SENT)
			.withContent("{}")
			.withCreatedAt(LocalDateTime.now())
			.build();

		// Act
		final var result = ApiMapper.toHistoryResponse(history);

		// Assert and verify
		assertThat(result).isNotNull();
		assertThat(result.messageType()).isEqualTo(messageType);
		assertThat(result.content()).isNotNull();
		assertThat(result.status()).isEqualTo(SENT);
		assertThat(result.timestamp()).isNotNull();
	}

	@Test
	void toMessageResult() {
		// Arrange
		final var history = List.of(
			History.builder()
				.withMessageId("11")
				.withDeliveryId("111")
				.withMessageType(SNAIL_MAIL)
				.withStatus(SENT)
				.build(),
			History.builder()
				.withMessageId("11")
				.withDeliveryId("222")
				.withMessageType(DIGITAL_MAIL)
				.withStatus(FAILED)
				.build());

		// Act
		final var result = ApiMapper.toMessageResult(history);

		// Assert and verify
		assertThat(result.messageId()).isEqualTo("11");
		assertThat(result.deliveries()).hasSize(2).satisfiesExactlyInAnyOrder(dr -> {
			assertThat(dr.deliveryId()).isEqualTo("111");
			assertThat(dr.messageType()).isEqualTo(SNAIL_MAIL);
			assertThat(dr.status()).isEqualTo(SENT);
		}, dr -> {
			assertThat(dr.deliveryId()).isEqualTo("222");
			assertThat(dr.messageType()).isEqualTo(DIGITAL_MAIL);
			assertThat(dr.status()).isEqualTo(FAILED);
		});
	}

	@Test
	void toMessageResultWhenMultipleRootEntries() {
		// Arrange
		final var history = List.of(
			History.builder()
				.withMessageId("1")
				.build(),
			History.builder()
				.withMessageId("2")
				.build());

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> ApiMapper.toMessageResult(history));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Unable to get message status");
	}

	@Test
	void toMessageBatchResult() {
		// Arrange
		final var history = List.of(
			History.builder()
				.withBatchId("1")
				.withMessageId("11")
				.withDeliveryId("111")
				.withMessageType(DIGITAL_MAIL)
				.withStatus(SENT)
				.build(),
			History.builder()
				.withBatchId("1")
				.withMessageId("22")
				.withDeliveryId("222")
				.withMessageType(SNAIL_MAIL)
				.withStatus(FAILED)
				.build());

		// Act
		final var result = ApiMapper.toMessageBatchResult(history);

		// Assert and verify
		assertThat(result.batchId()).isEqualTo("1");
		assertThat(result.messages()).hasSize(2).satisfiesExactlyInAnyOrder(mr -> {
			assertThat(mr.messageId()).isEqualTo("11");
			assertThat(mr.deliveries()).hasSize(1).allSatisfy(dr -> {
				assertThat(dr.deliveryId()).isEqualTo("111");
				assertThat(dr.messageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(dr.status()).isEqualTo(SENT);
			});
		}, mr -> {
			assertThat(mr.messageId()).isEqualTo("22");
			assertThat(mr.deliveries()).hasSize(1).allSatisfy(dr -> {
				assertThat(dr.deliveryId()).isEqualTo("222");
				assertThat(dr.messageType()).isEqualTo(SNAIL_MAIL);
				assertThat(dr.status()).isEqualTo(FAILED);
			});
		});
	}

	@Test
	void toMessageBatchResultWhenMultipleRootEntries() {
		// Arrange
		final var history = List.of(
			History.builder()
				.withBatchId("1")
				.withMessageId("1")
				.build(),
			History.builder()
				.withBatchId("2")
				.withMessageId("2")
				.build());

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> ApiMapper.toMessageBatchResult(history));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(Status.NOT_FOUND);
		assertThat(e.getMessage()).isEqualTo("Not Found: Unable to get batch status");
	}
}

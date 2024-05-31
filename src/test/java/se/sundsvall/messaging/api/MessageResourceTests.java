package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidLetterRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsBatchRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageResourceTests {

	private final InternalDeliveryResult deliveryResult = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withStatus(SENT)
		.build();

	private static final String ORIGIN = "origin";

	@Mock
	private MessageService mockMessageService;

	@Mock
	private MessageEventDispatcher mockEventDispatcher;

	@InjectMocks
	private MessageResource messageResource;

	@Test
	void sendSms() {
		final var request = createValidSmsRequest();
		when(mockMessageService.sendSms(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", SMS, SENT));

		final var response = messageResource.sendSms(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendSms(request.withOrigin(ORIGIN));
		verify(mockEventDispatcher, never()).handleSmsRequest(any(SmsRequest.class));
	}

	@Test
	void sendSmsAsync() {

		final var request = createValidSmsRequest();
		when(mockEventDispatcher.handleSmsRequest(request.withOrigin(ORIGIN)))
			.thenReturn(deliveryResult.withMessageType(SMS));

		final var response = messageResource.sendSms(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendSms(any(SmsRequest.class));
		verify(mockEventDispatcher).handleSmsRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendSmsBatch() {
		final var request = createValidSmsBatchRequest();
		final var deliveryBatchResult = InternalDeliveryBatchResult.builder()
			.withBatchId("someBatchId")
			.withDeliveries(List.of(InternalDeliveryResult.builder()
				.withMessageId("someMessageId")
				.withDeliveryId("someDeliveryId")
				.withMessageType(SMS)
				.withStatus(SENT)
				.build()))
			.build();

		when(mockEventDispatcher.handleSmsBatchRequest(request.withOrigin(ORIGIN))).thenReturn(deliveryBatchResult);

		final var response = messageResource.sendSmsBatch(ORIGIN, request);
		final var body = response.getBody();

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(body).isNotNull();

		assertThat(body.batchId()).isEqualTo("someBatchId");
		assertThat(body.messages()).isNotNull().hasSize(1);
		assertThat(body.messages().getFirst().messageId()).isEqualTo("someMessageId");
		assertThat(body.messages().getFirst().deliveries()).isNotNull().hasSize(1);
		assertThat(body.messages().getFirst().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(body.messages().getFirst().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(body.messages().getFirst().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleSmsBatchRequest(request.withOrigin(ORIGIN));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	@Test
	void sendEmail() {
		final var request = createValidEmailRequest();
		when(mockMessageService.sendEmail(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", EMAIL, SENT));

		final var response = messageResource.sendEmail(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendEmail(request.withOrigin(ORIGIN));
		verify(mockEventDispatcher, never()).handleEmailRequest(any(EmailRequest.class));
	}

	@Test
	void sendEmailAsync() {
		final var request = createValidEmailRequest();
		when(mockEventDispatcher.handleEmailRequest(request.withOrigin(ORIGIN)))
			.thenReturn(deliveryResult.withMessageType(EMAIL));

		final var response = messageResource.sendEmail(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendEmail(any(EmailRequest.class));
		verify(mockEventDispatcher).handleEmailRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendWebMessage() {
		final var request = createValidWebMessageRequest();
		when(mockMessageService.sendWebMessage(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", WEB_MESSAGE, SENT));

		final var response = messageResource.sendWebMessage(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendWebMessage(request.withOrigin(ORIGIN));
		verify(mockEventDispatcher, never()).handleWebMessageRequest(any(WebMessageRequest.class));
	}

	@Test
	void sendWebMessageAsync() {
		final var request = createValidWebMessageRequest();
		when(mockEventDispatcher.handleWebMessageRequest(request.withOrigin(ORIGIN)))
			.thenReturn(deliveryResult.withMessageType(WEB_MESSAGE));

		final var response = messageResource.sendWebMessage(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendWebMessage(any(WebMessageRequest.class));
		verify(mockEventDispatcher).handleWebMessageRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendDigitalMail() {
		final var request = createValidDigitalMailRequest();
		when(mockMessageService.sendDigitalMail(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", DIGITAL_MAIL, SENT))));

		final var response = messageResource.sendDigitalMail(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);

			});

		verify(mockMessageService).sendDigitalMail(request.withOrigin(ORIGIN));
		verify(mockEventDispatcher, never()).handleDigitalMailRequest(any(DigitalMailRequest.class));
	}

	@Test
	void sendDigitalMailAsync() {
		final var request = createValidDigitalMailRequest();
		when(mockEventDispatcher.handleDigitalMailRequest(request.withOrigin(ORIGIN)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(DIGITAL_MAIL)))
				.build());

		final var response = messageResource.sendDigitalMail(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
			});

		verify(mockMessageService, never()).sendDigitalMail(any(DigitalMailRequest.class));
		verify(mockEventDispatcher).handleDigitalMailRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendMessages() {
		final var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();

		when(mockMessageService.sendMessages(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", MESSAGE, SENT))));


		final var response = messageResource.sendMessages(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(MESSAGE);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
			});

		verify(mockMessageService).sendMessages(request.withOrigin(ORIGIN));
		verify(mockEventDispatcher, never()).handleMessageRequest(any(MessageRequest.class));
	}

	@Test
	void sendMessagesAsync() {
		final var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();
		when(mockEventDispatcher.handleMessageRequest(request.withOrigin(ORIGIN)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(MESSAGE)))
				.build());

		final var response = messageResource.sendMessages(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(MESSAGE);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
			});

		verify(mockMessageService, never()).sendMessages(any(MessageRequest.class));
		verify(mockEventDispatcher).handleMessageRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendLetter() {
		final var request = createValidLetterRequest();
		when(mockMessageService.sendLetter(request.withOrigin(ORIGIN)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", LETTER, SENT))));

		var response = messageResource.sendLetter(ORIGIN, request, false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(LETTER);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
			});

		verify(mockMessageService).sendLetter(any(LetterRequest.class));
		verify(mockEventDispatcher, never()).handleLetterRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void sendLetterAsync() {
		final var request = createValidLetterRequest();
		when(mockEventDispatcher.handleLetterRequest(request.withOrigin(ORIGIN)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(LETTER)))
				.build());

		final var response = messageResource.sendLetter(ORIGIN, request, true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
		assertThat(response.getBody().messages())
			.hasSize(1)
			.allSatisfy(messageResult -> {
				assertThat(messageResult.messageId()).isEqualTo("someMessageId");
				assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
				assertThat(messageResult.deliveries().getFirst().messageType()).isEqualTo(LETTER);
				assertThat(messageResult.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
				assertThat(messageResult.deliveries().getFirst().status()).isEqualTo(SENT);
			});

		verify(mockMessageService, never()).sendLetter(any(LetterRequest.class));
		verify(mockEventDispatcher).handleLetterRequest(request.withOrigin(ORIGIN));
	}

	@Test
	void toResponse_fromDeliveryResult() {
		final var deliveryResult = InternalDeliveryResult.builder()
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withMessageType(DIGITAL_MAIL)
			.withStatus(SENT)
			.build();

		final var result = messageResource.toResponse(deliveryResult);

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
					.build()
			))
			.build();

		final var result = messageResource.toResponse(deliveryBatchResult);

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
}

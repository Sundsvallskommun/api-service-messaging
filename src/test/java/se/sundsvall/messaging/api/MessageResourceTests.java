package se.sundsvall.messaging.api;

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
import se.sundsvall.messaging.service.BlacklistService;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidLetterRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
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
	@Mock
	private BlacklistService mockBlacklist;

	@InjectMocks
	private MessageResource messageResource;

	@Test
	void sendSms() {
		when(mockMessageService.sendSms(anyString(), any(SmsRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", SMS, SENT));

		var response = messageResource.sendSms(ORIGIN, createValidSmsRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendSms(anyString(), any(SmsRequest.class));
		verify(mockEventDispatcher, never()).handleSmsRequest(anyString(), any(SmsRequest.class));
	}

	@Test
	void sendSmsAsync() {

		when(mockEventDispatcher.handleSmsRequest(anyString(), any(SmsRequest.class)))
			.thenReturn(deliveryResult.withMessageType(SMS));

		var response = messageResource.sendSms(ORIGIN, createValidSmsRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendSms(anyString(), any(SmsRequest.class));
		verify(mockEventDispatcher).handleSmsRequest(anyString(), any(SmsRequest.class));
	}

	@Test
	void sendEmail() {
		when(mockMessageService.sendEmail(anyString(), any(EmailRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", EMAIL, SENT));

		var response = messageResource.sendEmail(ORIGIN, createValidEmailRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendEmail(anyString(), any(EmailRequest.class));
		verify(mockEventDispatcher, never()).handleEmailRequest(anyString(), any(EmailRequest.class));
	}

	@Test
	void sendEmailAsync() {
		when(mockEventDispatcher.handleEmailRequest(anyString(), any(EmailRequest.class)))
			.thenReturn(deliveryResult.withMessageType(EMAIL));

		var response = messageResource.sendEmail(ORIGIN, createValidEmailRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendEmail(anyString(), any(EmailRequest.class));
		verify(mockEventDispatcher).handleEmailRequest(anyString(), any(EmailRequest.class));
	}

	@Test
	void sendWebMessage() {
		when(mockMessageService.sendWebMessage(anyString(), any(WebMessageRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", WEB_MESSAGE, SENT));

		var response = messageResource.sendWebMessage(ORIGIN, createValidWebMessageRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendWebMessage(anyString(), any(WebMessageRequest.class));
		verify(mockEventDispatcher, never()).handleWebMessageRequest(anyString(), any(WebMessageRequest.class));
	}

	@Test
	void sendWebMessageAsync() {
		when(mockEventDispatcher.handleWebMessageRequest(anyString(), any(WebMessageRequest.class)))
			.thenReturn(deliveryResult.withMessageType(WEB_MESSAGE));

		var response = messageResource.sendWebMessage(ORIGIN, createValidWebMessageRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendWebMessage(anyString(), any(WebMessageRequest.class));
		verify(mockEventDispatcher).handleWebMessageRequest(anyString(), any(WebMessageRequest.class));
	}

	@Test
	void sendDigitalMail() {
		when(mockMessageService.sendDigitalMail(anyString(), any(DigitalMailRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", DIGITAL_MAIL, SENT))));

		var response = messageResource.sendDigitalMail(ORIGIN, createValidDigitalMailRequest(), false);

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

		verify(mockMessageService).sendDigitalMail(anyString(), any(DigitalMailRequest.class));
		verify(mockEventDispatcher, never()).handleDigitalMailRequest(anyString(), any(DigitalMailRequest.class));
	}

	@Test
	void sendDigitalMailAsync() {
		when(mockEventDispatcher.handleDigitalMailRequest(anyString(), any(DigitalMailRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(DIGITAL_MAIL)))
				.build());

		var response = messageResource.sendDigitalMail(ORIGIN, createValidDigitalMailRequest(), true);

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

		verify(mockMessageService, never()).sendDigitalMail(anyString(), any(DigitalMailRequest.class));
		verify(mockEventDispatcher).handleDigitalMailRequest(anyString(), any(DigitalMailRequest.class));
	}

	@Test
	void sendMessages() {
		when(mockMessageService.sendMessages(anyString(), any(MessageRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", MESSAGE, SENT))));

		var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();

		var response = messageResource.sendMessages(ORIGIN, request, false);

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

		verify(mockMessageService).sendMessages(anyString(), any(MessageRequest.class));
		verify(mockEventDispatcher, never()).handleMessageRequest(anyString(), any(MessageRequest.class));
	}

	@Test
	void sendMessagesAsync() {
		when(mockEventDispatcher.handleMessageRequest(anyString(), any(MessageRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(MESSAGE)))
				.build());
		var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();

		var response = messageResource.sendMessages(ORIGIN, request, true);

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

		verify(mockMessageService, never()).sendMessages(anyString(), any(MessageRequest.class));
		verify(mockEventDispatcher).handleMessageRequest(anyString(), any(MessageRequest.class));
	}

	@Test
	void sendLetter() {
		when(mockMessageService.sendLetter(anyString(), any(LetterRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", LETTER, SENT))));

		var response = messageResource.sendLetter(ORIGIN, createValidLetterRequest(), false);

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

		verify(mockMessageService).sendLetter(anyString(), any(LetterRequest.class));
		verify(mockEventDispatcher, never()).handleLetterRequest(anyString(), any(LetterRequest.class));
	}

	@Test
	void sendLetterAsync() {
		when(mockEventDispatcher.handleLetterRequest(anyString(), any(LetterRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(LETTER)))
				.build());

		var response = messageResource.sendLetter(ORIGIN, createValidLetterRequest(), true);

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

		verify(mockMessageService, never()).sendLetter(anyString(), any(LetterRequest.class));
		verify(mockEventDispatcher).handleLetterRequest(anyString(), any(LetterRequest.class));
	}

	@Test
	void toResponse_fromDeliveryResult() {
		var deliveryResult = InternalDeliveryResult.builder()
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withMessageType(DIGITAL_MAIL)
			.withStatus(SENT)
			.build();

		var result = messageResource.toResponse(deliveryResult);

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

		var result = messageResource.toResponse(deliveryBatchResult);

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

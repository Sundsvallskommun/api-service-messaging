package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
import se.sundsvall.messaging.service.BlacklistService;
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

	@Mock
	private MessageService mockMessageService;
	@Mock
	private MessageEventDispatcher mockEventDispatcher;
	@Mock
	private BlacklistService mockBlacklist;

	@InjectMocks
	private MessageResource messageResource;

	@Test
	void test_sendSms() {
		when(mockMessageService.sendSms(any(SmsRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", SMS, SENT));

		var response = messageResource.sendSms(createValidSmsRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, times(1)).sendSms(any(SmsRequest.class));
		verify(mockEventDispatcher, never()).handleSmsRequest(any(SmsRequest.class));
	}

	@Test
	void test_sendSmsAsync() {
		when(mockEventDispatcher.handleSmsRequest(any(SmsRequest.class)))
			.thenReturn(deliveryResult.withMessageType(SMS));

		var response = messageResource.sendSms(createValidSmsRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(SMS);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendSms(any(SmsRequest.class));
		verify(mockEventDispatcher, times(1)).handleSmsRequest(any(SmsRequest.class));
	}

	@Test
	void test_sendEmail() {
		when(mockMessageService.sendEmail(any(EmailRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", EMAIL, SENT));

		var response = messageResource.sendEmail(createValidEmailRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, times(1)).sendEmail(any(EmailRequest.class));
		verify(mockEventDispatcher, never()).handleEmailRequest(any(EmailRequest.class));
	}

	@Test
	void test_sendEmailAsync() {
		when(mockEventDispatcher.handleEmailRequest(any(EmailRequest.class)))
			.thenReturn(deliveryResult.withMessageType(EMAIL));

		var response = messageResource.sendEmail(createValidEmailRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendEmail(any(EmailRequest.class));
		verify(mockEventDispatcher, times(1)).handleEmailRequest(any(EmailRequest.class));
	}

	@Test
	void test_sendWebMessage() {
		when(mockMessageService.sendWebMessage(any(WebMessageRequest.class)))
			.thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", WEB_MESSAGE, SENT));

		var response = messageResource.sendWebMessage(createValidWebMessageRequest(), false);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, times(1)).sendWebMessage(any(WebMessageRequest.class));
		verify(mockEventDispatcher, never()).handleWebMessageRequest(any(WebMessageRequest.class));
	}

	@Test
	void test_sendWebMessageAsync() {
		when(mockEventDispatcher.handleWebMessageRequest(any(WebMessageRequest.class)))
			.thenReturn(deliveryResult.withMessageType(WEB_MESSAGE));

		var response = messageResource.sendWebMessage(createValidWebMessageRequest(), true);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(CREATED);
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
		assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
		assertThat(response.getBody().deliveries().getFirst().messageType()).isEqualTo(WEB_MESSAGE);
		assertThat(response.getBody().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.getBody().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService, never()).sendWebMessage(any(WebMessageRequest.class));
		verify(mockEventDispatcher, times(1)).handleWebMessageRequest(any(WebMessageRequest.class));
	}

	@Test
	void test_sendDigitalMail() {
		when(mockMessageService.sendDigitalMail(any(DigitalMailRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", DIGITAL_MAIL, SENT))));

		var response = messageResource.sendDigitalMail(createValidDigitalMailRequest(), false);

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

		verify(mockMessageService, times(1)).sendDigitalMail(any(DigitalMailRequest.class));
		verify(mockEventDispatcher, never()).handleDigitalMailRequest(any(DigitalMailRequest.class));
	}

	@Test
	void test_sendDigitalMailAsync() {
		when(mockEventDispatcher.handleDigitalMailRequest(any(DigitalMailRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(DIGITAL_MAIL)))
				.build());

		var response = messageResource.sendDigitalMail(createValidDigitalMailRequest(), true);

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
		verify(mockEventDispatcher, times(1)).handleDigitalMailRequest(any(DigitalMailRequest.class));
	}

	@Test
	void test_sendMessages() {
		when(mockMessageService.sendMessages(any(MessageRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", MESSAGE, SENT))));

		var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();

		var response = messageResource.sendMessages(request, false);

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

		verify(mockMessageService, times(1)).sendMessages(any(MessageRequest.class));
		verify(mockEventDispatcher, never()).handleMessageRequest(any(MessageRequest.class));
	}

	@Test
	void test_sendMessagesAsync() {
		when(mockEventDispatcher.handleMessageRequest(any(MessageRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(MESSAGE)))
				.build());
		var request = MessageRequest.builder()
			.withMessages(List.of(createValidMessageRequestMessage()))
			.build();

		var response = messageResource.sendMessages(request, true);

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
		verify(mockEventDispatcher, times(1)).handleMessageRequest(any(MessageRequest.class));
	}

	@Test
	void test_sendLetter() {
		when(mockMessageService.sendLetter(any(LetterRequest.class)))
			.thenReturn(new InternalDeliveryBatchResult("someBatchId",
				List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", LETTER, SENT))));

		var response = messageResource.sendLetter(createValidLetterRequest(), false);

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

		verify(mockMessageService, times(1)).sendLetter(any(LetterRequest.class));
		verify(mockEventDispatcher, never()).handleLetterRequest(any(LetterRequest.class));
	}

	@Test
	void test_sendLetterAsync() {
		when(mockEventDispatcher.handleLetterRequest(any(LetterRequest.class)))
			.thenReturn(InternalDeliveryBatchResult.builder()
				.withBatchId("someBatchId")
				.withDeliveries(List.of(deliveryResult.withMessageType(LETTER)))
				.build());

		var response = messageResource.sendLetter(createValidLetterRequest(), true);

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
		verify(mockEventDispatcher, times(1)).handleLetterRequest(any(LetterRequest.class));
	}

	@Test
	void test_toResponse_fromDeliveryResult() {
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

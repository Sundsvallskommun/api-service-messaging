package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
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
import org.springframework.web.util.UriComponentsBuilder;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
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

    private final UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

    @Mock
    private MessageService mockMessageService;
    @Mock
    private MessageEventDispatcher mockEventDispatcher;

    @InjectMocks
    private MessageResource messageResource;

    @Test
    void test_sendSms() {
        when(mockMessageService.sendSms(any(SmsRequest.class)))
            .thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", SMS, SENT));

        var response = messageResource.sendSms(SmsRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(SMS);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendSms(any(SmsRequest.class));
        verify(mockEventDispatcher, never()).handleSmsRequest(any(SmsRequest.class));
    }

    @Test
    void test_sendSmsAsync() {
        when(mockEventDispatcher.handleSmsRequest(any(SmsRequest.class)))
            .thenReturn(deliveryResult.withMessageType(SMS));

        var response = messageResource.sendSms(SmsRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(SMS);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, never()).sendSms(any(SmsRequest.class));
        verify(mockEventDispatcher, times(1)).handleSmsRequest(any(SmsRequest.class));
    }

    @Test
    void test_sendEmail() {
        when(mockMessageService.sendEmail(any(EmailRequest.class)))
            .thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", EMAIL, SENT));

        var response = messageResource.sendEmail(EmailRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(EMAIL);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendEmail(any(EmailRequest.class));
        verify(mockEventDispatcher, never()).handleEmailRequest(any(EmailRequest.class));
    }

    @Test
    void test_sendEmailAsync() {
        when(mockEventDispatcher.handleEmailRequest(any(EmailRequest.class)))
            .thenReturn(deliveryResult.withMessageType(EMAIL));

        var response = messageResource.sendEmail(EmailRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(EMAIL);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, never()).sendEmail(any(EmailRequest.class));
        verify(mockEventDispatcher, times(1)).handleEmailRequest(any(EmailRequest.class));
    }

    @Test
    void test_sendWebMessage() {
        when(mockMessageService.sendWebMessage(any(WebMessageRequest.class)))
            .thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", WEB_MESSAGE, SENT));

        var response = messageResource.sendWebMessage(WebMessageRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(WEB_MESSAGE);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendWebMessage(any(WebMessageRequest.class));
        verify(mockEventDispatcher, never()).handleWebMessageRequest(any(WebMessageRequest.class));
    }

    @Test
    void test_sendWebMessageAsync() {
        when(mockEventDispatcher.handleWebMessageRequest(any(WebMessageRequest.class)))
            .thenReturn(deliveryResult.withMessageType(WEB_MESSAGE));

        var response = messageResource.sendWebMessage(WebMessageRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(WEB_MESSAGE);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, never()).sendWebMessage(any(WebMessageRequest.class));
        verify(mockEventDispatcher, times(1)).handleWebMessageRequest(any(WebMessageRequest.class));
    }

    @Test
    void test_sendDigitalMail() {
        when(mockMessageService.sendDigitalMail(any(DigitalMailRequest.class)))
            .thenReturn(new InternalDeliveryBatchResult("someBatchId",
                List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", DIGITAL_MAIL, SENT))));

        var response = messageResource.sendDigitalMail(DigitalMailRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(DIGITAL_MAIL);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);

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

        var response = messageResource.sendDigitalMail(DigitalMailRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(DIGITAL_MAIL);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);
            });

        verify(mockMessageService, never()).sendDigitalMail(any(DigitalMailRequest.class));
        verify(mockEventDispatcher, times(1)).handleDigitalMailRequest(any(DigitalMailRequest.class));
    }

    @Test
    void test_sendMessages() {
        when(mockMessageService.sendMessages(any(MessageRequest.class)))
            .thenReturn(new InternalDeliveryBatchResult("someBatchId",
                List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", MESSAGE, SENT))));

        var response = messageResource.sendMessages(MessageRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(MESSAGE);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);
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

        var response = messageResource.sendMessages(MessageRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(MESSAGE);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);
            });

        verify(mockMessageService, never()).sendMessages(any(MessageRequest.class));
        verify(mockEventDispatcher, times(1)).handleMessageRequest(any(MessageRequest.class));
    }

    @Test
    void test_sendSnailMail() {
        when(mockMessageService.sendSnailMail(any(SnailMailRequest.class)))
            .thenReturn(new InternalDeliveryResult("someMessageId", "someDeliveryId", SNAIL_MAIL, SENT));

        var response = messageResource.sendSnailMail(SnailMailRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendSnailMail(any(SnailMailRequest.class));
        verify(mockEventDispatcher, never()).handleSnailMailRequest(any(SnailMailRequest.class));
    }

    @Test
    void test_sendSnailMailAsync() {
        when(mockEventDispatcher.handleSnailMailRequest(any(SnailMailRequest.class)))
            .thenReturn(deliveryResult.withMessageType(SNAIL_MAIL));

        var response = messageResource.sendSnailMail(SnailMailRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveries()).isNotNull().hasSize(1);
        assertThat(response.getBody().deliveries().get(0).messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(response.getBody().deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().deliveries().get(0).status()).isEqualTo(SENT);

        verify(mockMessageService, never()).sendSnailMail(any(SnailMailRequest.class));
        verify(mockEventDispatcher, times(1)).handleSnailMailRequest(any(SnailMailRequest.class));
    }

    @Test
    void test_sendLetter() {
        when(mockMessageService.sendLetter(any(LetterRequest.class)))
            .thenReturn(new InternalDeliveryBatchResult("someBatchId",
                List.of(new InternalDeliveryResult("someMessageId", "someDeliveryId", LETTER, SENT))));

        var response = messageResource.sendLetter(LetterRequest.builder().build(), false, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(LETTER);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);
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

        var response = messageResource.sendLetter(LetterRequest.builder().build(), true, uriComponentsBuilder);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
                assertThat(messageResult.deliveries().get(0).messageType()).isEqualTo(LETTER);
                assertThat(messageResult.deliveries().get(0).deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.deliveries().get(0).status()).isEqualTo(SENT);
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

        var result = messageResource.toResponse(uriComponentsBuilder, deliveryResult);

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

        var result = messageResource.toResponse(uriComponentsBuilder, deliveryBatchResult);

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

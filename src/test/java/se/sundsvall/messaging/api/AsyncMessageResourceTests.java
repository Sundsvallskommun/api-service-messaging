package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

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
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.model.DeliveryBatchResult;
import se.sundsvall.messaging.model.DeliveryResult;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class AsyncMessageResourceTests {

    @Mock
    private MessageEventDispatcher mockEventDispatcher;

    @InjectMocks
    private AsyncMessageResource messageResource;

    private final DeliveryResult deliveryResult = DeliveryResult.builder()
        .withMessageId("someMessageId")
        .withDeliveryId("someDeliveryId")
        .withStatus(MessageStatus.SENT)
        .build();


    @Test
    void test_sendSms() {
        when(mockEventDispatcher.handleSmsRequest(any(SmsRequest.class)))
            .thenReturn(deliveryResult);

        var response = messageResource.sendSms(SmsRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockEventDispatcher, times(1)).handleSmsRequest(any(SmsRequest.class));
    }

    @Test
    void test_sendEmail() {
        when(mockEventDispatcher.handleEmailRequest(any(EmailRequest.class)))
            .thenReturn(deliveryResult);

        var response = messageResource.sendEmail(EmailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockEventDispatcher, times(1)).handleEmailRequest(any(EmailRequest.class));
    }

    @Test
    void test_sendWebMessage() {
        when(mockEventDispatcher.handleWebMessageRequest(any(WebMessageRequest.class)))
            .thenReturn(deliveryResult);

        var response = messageResource.sendWebMessage(WebMessageRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockEventDispatcher, times(1)).handleWebMessageRequest(any(WebMessageRequest.class));
    }

    @Test
    void test_sendDigitalMail() {
        when(mockEventDispatcher.handleDigitalMailRequest(any(DigitalMailRequest.class)))
            .thenReturn(DeliveryBatchResult.builder()
                .withBatchId("someBatchId")
                .withDeliveries(List.of(deliveryResult))
                .build());

        var response = messageResource.sendDigitalMail(DigitalMailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.status()).isEqualTo(SENT);
            });

        verify(mockEventDispatcher, times(1)).handleDigitalMailRequest(any(DigitalMailRequest.class));
    }

    @Test
    void test_sendMessages() {
        when(mockEventDispatcher.handleMessageRequest(any(MessageRequest.class)))
            .thenReturn(DeliveryBatchResult.builder()
                .withBatchId("someBatchId")
                .withDeliveries(List.of(deliveryResult))
                .build());

        var response = messageResource.sendMessages(MessageRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.status()).isEqualTo(SENT);
            });

        verify(mockEventDispatcher, times(1)).handleMessageRequest(any(MessageRequest.class));
    }

    @Test
    void test_sendSnailMail() {
        when(mockEventDispatcher.handleSnailMailRequest(any(SnailMailRequest.class)))
            .thenReturn(deliveryResult);

        var response = messageResource.sendSnailMail(SnailMailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockEventDispatcher, times(1)).handleSnailMailRequest(any(SnailMailRequest.class));
    }

    @Test
    void test_sendLetter() {
        when(mockEventDispatcher.handleLetterRequest(any(LetterRequest.class)))
            .thenReturn(DeliveryBatchResult.builder()
                .withBatchId("someBatchId")
                .withDeliveries(List.of(deliveryResult))
                .build());

        var response = messageResource.sendLetter(LetterRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().batchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().messages())
            .hasSize(1)
            .allSatisfy(messageResult -> {
                assertThat(messageResult.messageId()).isEqualTo("someMessageId");
                assertThat(messageResult.deliveryId()).isEqualTo("someDeliveryId");
                assertThat(messageResult.status()).isEqualTo(SENT);
            });

        verify(mockEventDispatcher, times(1)).handleLetterRequest(any(LetterRequest.class));
    }
}

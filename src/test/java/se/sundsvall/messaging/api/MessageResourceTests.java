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
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageResourceTests {

    @Mock
    private MessageService mockMessageService;

    @InjectMocks
    private MessageResource messageResource;

    @Test
    void test_sendSms() {
        when(mockMessageService.sendSms(any(SmsRequest.class)))
            .thenReturn(new DeliveryResult("someMessageId", "someDeliveryId", SENT));

        var response = messageResource.sendSms(SmsRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendSms(any(SmsRequest.class));
    }

    @Test
    void test_sendEmail() {
        when(mockMessageService.sendEmail(any(EmailRequest.class)))
            .thenReturn(new DeliveryResult("someMessageId", "someDeliveryId", SENT));

        var response = messageResource.sendEmail(EmailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendEmail(any(EmailRequest.class));
    }

    @Test
    void test_sendWebMessage() {
        when(mockMessageService.sendWebMessage(any(WebMessageRequest.class)))
            .thenReturn(new DeliveryResult("someMessageId", "someDeliveryId", SENT));

        var response = messageResource.sendWebMessage(WebMessageRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendWebMessage(any(WebMessageRequest.class));
    }

    @Test
    void test_sendDigitalMail() {
        when(mockMessageService.sendDigitalMail(any(DigitalMailRequest.class)))
            .thenReturn(new DeliveryBatchResult("someBatchId",
                List.of(new DeliveryResult("someMessageId", "someDeliveryId", SENT))));

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

        verify(mockMessageService, times(1)).sendDigitalMail(any(DigitalMailRequest.class));
    }

    @Test
    void test_sendMessages() {
        when(mockMessageService.sendMessages(any(MessageRequest.class)))
            .thenReturn(new DeliveryBatchResult("someBatchId",
                List.of(new DeliveryResult("someMessageId", "someDeliveryId", SENT))));

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

        verify(mockMessageService, times(1)).sendMessages(any(MessageRequest.class));
    }

    @Test
    void test_sendSnailMail() {
        when(mockMessageService.sendSnailMail(any(SnailMailRequest.class)))
            .thenReturn(new DeliveryResult("someMessageId", "someDeliveryId", SENT));

        var response = messageResource.sendSnailMail(SnailMailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().messageId()).isEqualTo("someMessageId");
        assertThat(response.getBody().deliveryId()).isEqualTo("someDeliveryId");
        assertThat(response.getBody().status()).isEqualTo(SENT);

        verify(mockMessageService, times(1)).sendSnailMail(any(SnailMailRequest.class));
    }

    @Test
    void test_sendLetter() {
        when(mockMessageService.sendLetter(any(LetterRequest.class)))
            .thenReturn(new DeliveryBatchResult("someBatchId",
                List.of(new DeliveryResult("someMessageId", "someDeliveryId", SENT))));

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

        verify(mockMessageService, times(1)).sendLetter(any(LetterRequest.class));
    }
}

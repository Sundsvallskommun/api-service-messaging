package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.api.model.DigitalMailRequest;
import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.api.model.LetterRequest;
import se.sundsvall.messaging.api.model.MessageRequest;
import se.sundsvall.messaging.api.model.SmsRequest;
import se.sundsvall.messaging.api.model.SnailmailRequest;
import se.sundsvall.messaging.api.model.WebMessageRequest;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.dto.MessageDto;
import se.sundsvall.messaging.service.MessageService;

@ExtendWith(MockitoExtension.class)
class MessageResourceTests {

    @Mock
    private MessageService mockMessageService;

    private MessageResource messageResource;

    @BeforeEach
    void setUp() {
        messageResource = new MessageResource(mockMessageService);
    }

    @Test
    void test_sendSms() {
        when(mockMessageService.handleSmsRequest(any(SmsRequest.class)))
            .thenReturn(MessageDto.builder()
                .withMessageId("someMessageId")
                .build());

        var response = messageResource.sendSms(SmsRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageId()).isEqualTo("someMessageId");

        verify(mockMessageService, times(1)).handleSmsRequest(any(SmsRequest.class));
    }

    @Test
    void test_sendEmail() {
        when(mockMessageService.handleEmailRequest(any(EmailRequest.class)))
            .thenReturn(MessageDto.builder()
                .withMessageId("someMessageId")
                .build());

        var response = messageResource.sendEmail(EmailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageId()).isEqualTo("someMessageId");

        verify(mockMessageService, times(1)).handleEmailRequest(any(EmailRequest.class));
    }

    @Test
    void test_sendWebMessage() {
        when(mockMessageService.handleWebMessageRequest(any(WebMessageRequest.class)))
            .thenReturn(MessageDto.builder()
                .withMessageId("someMessageId")
                .build());

        var response = messageResource.sendWebMessage(
            WebMessageRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageId()).isEqualTo("someMessageId");

        verify(mockMessageService, times(1)).handleWebMessageRequest(any(WebMessageRequest.class));
    }

    @Test
    void test_sendDigitalMail() {
        when(mockMessageService.handleDigitalMailRequest(any(DigitalMailRequest.class)))
            .thenReturn(MessageBatchDto.builder()
                .withMessageIds(List.of("someMessageId"))
                .build());

        var response = messageResource.sendDigitalMail(
            DigitalMailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageIds()).contains("someMessageId");

        verify(mockMessageService, times(1)).handleDigitalMailRequest(any(DigitalMailRequest.class));
    }

    @Test
    void test_sendMessages() {
        when(mockMessageService.handleMessageRequest(any(MessageRequest.class)))
            .thenReturn(MessageBatchDto.builder()
                .withBatchId("someBatchId")
                .withMessageIds(List.of("someMessageId"))
                .build());

        var response = messageResource.sendMessages(
                MessageRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBatchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().getMessageIds()).containsExactly("someMessageId");

        verify(mockMessageService, times(1)).handleMessageRequest(any(MessageRequest.class));
    }

    @Test
    void test_sendSnailMail() {
        when(mockMessageService.handleSnailmailRequest(any(SnailmailRequest.class)))
                .thenReturn(MessageDto.builder()
                        .withMessageId("someMessageId")
                        .build());

        var response = messageResource.sendSnailmail(SnailmailRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessageId()).isEqualTo("someMessageId");

        verify(mockMessageService, times(1)).handleSnailmailRequest(any(SnailmailRequest.class));
    }

    @Test
    void test_sendLetter() {
        when(mockMessageService.handleLetterRequest(any(LetterRequest.class)))
                .thenReturn(MessageBatchDto.builder()
                        .withBatchId("someBatchId")
                        .withMessageIds(List.of("someMessageId"))
                        .build());

        var response = messageResource.sendLetter(
                LetterRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getBatchId()).isEqualTo("someBatchId");
        assertThat(response.getBody().getMessageIds()).containsExactly("someMessageId");

        verify(mockMessageService, times(1)).handleLetterRequest(any(LetterRequest.class));
    }
}

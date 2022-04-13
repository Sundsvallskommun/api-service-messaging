package se.sundsvall.messaging.service.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;

import java.util.Optional;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;

@ExtendWith(MockitoExtension.class)
class EmailProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private EmailSenderIntegration mockEmailSenderIntegration;

    private EmailProcessor emailProcessor;

    @BeforeEach
    void setUp() {
        var backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(10);
        backOffPolicy.setMultiplier(2);

        var retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(5));
        retryTemplate.setBackOffPolicy(backOffPolicy);

        emailProcessor = new EmailProcessor(retryTemplate, mockMessageRepository,
            mockHistoryRepository, mockEmailSenderIntegration);
    }

    @Test
    void testHandleIncomingEmailEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findById(any(String.class))).thenReturn(Optional.empty());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findById(any(String.class));
        verify(mockEmailSenderIntegration, never()).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, never()).deleteById(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(ResponseEntity.ok().build());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockEmailSenderIntegration, times(1)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent_whenEmailSenderIntegrationThrowsException() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenThrow(RuntimeException.class);

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockEmailSenderIntegration, times(5)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent_whenEmailSenderIntegrationReturnsFailure() {
        var emailRequest = createEmailRequest();
        var messageId = UUID.randomUUID().toString();

        when(mockMessageRepository.findById(eq(messageId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageId));

        verify(mockMessageRepository, times(1)).findById(eq(messageId));
        verify(mockEmailSenderIntegration, times(5)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteById(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var emailRequest = createEmailRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(emailRequest.getParty().getPartyId())
            .withType(MessageType.EMAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(emailRequest))
            .build();

        var dto = emailProcessor.mapToDto(message);

        assertThat(dto.getSender()).isEqualTo(emailRequest.getSender());
        assertThat(dto.getEmailAddress()).isEqualTo(emailRequest.getEmailAddress());
        assertThat(dto.getSubject()).isEqualTo(emailRequest.getSubject());
        assertThat(dto.getMessage()).isEqualTo(emailRequest.getMessage());
        assertThat(dto.getHtmlMessage()).isEqualTo(emailRequest.getHtmlMessage());
        assertThat(dto.getAttachments()).hasSameSizeAs(emailRequest.getAttachments());
    }
}

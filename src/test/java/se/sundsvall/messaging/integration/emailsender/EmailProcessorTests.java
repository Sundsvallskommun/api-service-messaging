package se.sundsvall.messaging.integration.emailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;

import java.time.Duration;
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

import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.EmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingEmailEvent;

@ExtendWith(MockitoExtension.class)
class EmailProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private RetryProperties mockRetryProperties;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private EmailSenderIntegration mockEmailSenderIntegration;
    @Mock
    private DefaultSettings mockDefaultSettings;

    private EmailProcessor emailProcessor;

    @BeforeEach
    void setUp() {
        when(mockRetryProperties.getMaxAttempts()).thenReturn(3);
        when(mockRetryProperties.getInitialDelay()).thenReturn(Duration.ofMillis(1));
        when(mockRetryProperties.getMaxDelay()).thenReturn(Duration.ofMillis(100));

        emailProcessor = new EmailProcessor(mockRetryProperties, mockMessageRepository,
            mockHistoryRepository, mockEmailSenderIntegration, mockDefaultSettings);
    }

    @Test
    void testHandleIncomingEmailEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
        verify(mockEmailSenderIntegration, never()).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent() {
        var emailRequest = createEmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(ResponseEntity.ok().build());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockEmailSenderIntegration, times(1)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent_whenEmailSenderIntegrationThrowsException() {
        var emailRequest = createEmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenThrow(RuntimeException.class);

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockEmailSenderIntegration, times(3)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingEmailEvent_whenEmailSenderIntegrationReturnsFailure() {
        var emailRequest = createEmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(emailRequest.getParty().getPartyId())
                .withType(MessageType.EMAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(emailRequest))
                .build()));
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        emailProcessor.handleIncomingEmailEvent(new IncomingEmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockEmailSenderIntegration, times(3)).sendEmail(any(EmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
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

package se.sundsvall.messaging.integration.snailmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createSnailmailRequest;

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

import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.SnailmailDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingSnailmailEvent;


@ExtendWith(MockitoExtension.class)
public class SnailmailProcessorTests {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private RetryProperties mockRetryProperties;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private CounterRepository mockCounterRepository;
    @Mock
    private SnailmailSenderIntegration mockSnailmailSenderIntegration;

    private SnailmailProcessor snailmailProcessor;

    @BeforeEach
    void setUp() {
        when(mockRetryProperties.getMaxAttempts()).thenReturn(3);
        when(mockRetryProperties.getInitialDelay()).thenReturn(Duration.ofMillis(1));
        when(mockRetryProperties.getMaxDelay()).thenReturn(Duration.ofMillis(100));

        snailmailProcessor = new SnailmailProcessor(mockRetryProperties, mockMessageRepository,
            mockHistoryRepository, mockCounterRepository, mockSnailmailSenderIntegration);
    }

    @Test
    void testHandleIncomingSnailmailEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        snailmailProcessor.handleIncomingSnailmailEvent(new IncomingSnailmailEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
        verify(mockSnailmailSenderIntegration, never()).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSnailmailEvent() {
        var snailmailRequest = createSnailmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(snailmailRequest.getParty().getPartyId())
                        .withType(MessageType.SNAIL_MAIL)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(snailmailRequest))
                        .build()));
        when(mockSnailmailSenderIntegration.sendSnailmail(any(SnailmailDto.class))).thenReturn(ResponseEntity.ok().build());

        snailmailProcessor.handleIncomingSnailmailEvent(new IncomingSnailmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockSnailmailSenderIntegration, times(1)).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSnailmailEvent_whenSnailmailSenderIntegrationThrowsException() {
        var snailmailRequest = createSnailmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(snailmailRequest.getParty().getPartyId())
                        .withType(MessageType.SNAIL_MAIL)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(snailmailRequest))
                        .build()));
        when(mockSnailmailSenderIntegration.sendSnailmail(any(SnailmailDto.class))).thenThrow(RuntimeException.class);

        snailmailProcessor.handleIncomingSnailmailEvent(new IncomingSnailmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockSnailmailSenderIntegration, times(3)).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingSnailmailEvent_whenSnailmailSenderIntegrationReturnsFailure() {
        var snailmailRequest = createSnailmailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(snailmailRequest.getParty().getPartyId())
                        .withType(MessageType.SNAIL_MAIL)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(snailmailRequest))
                        .build()));
        when(mockSnailmailSenderIntegration.sendSnailmail(any(SnailmailDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        snailmailProcessor.handleIncomingSnailmailEvent(new IncomingSnailmailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockSnailmailSenderIntegration, times(3)).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        var snailmailRequest = createSnailmailRequest();

        var message = MessageEntity.builder()
                .withMessageId(UUID.randomUUID().toString())
                .withPartyId(snailmailRequest.getParty().getPartyId())
                .withType(MessageType.SNAIL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(snailmailRequest))
                .build();

        var dto = snailmailProcessor.mapToDto(message);

        assertThat(dto.getPersonId()).isEqualTo(snailmailRequest.getPersonId());
        assertThat(dto.getAttachments()).hasSameSizeAs(snailmailRequest.getAttachments());
    }
}

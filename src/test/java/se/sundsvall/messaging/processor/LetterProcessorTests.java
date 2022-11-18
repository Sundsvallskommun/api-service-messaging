package se.sundsvall.messaging.processor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createLetterRequest;
import static se.sundsvall.messaging.api.model.LetterRequest.DeliveryMode.DIGITAL;
import static se.sundsvall.messaging.processor.Processor.GSON;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import se.sundsvall.messaging.api.model.LetterRequest;
import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.dto.SnailmailDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailmailSenderIntegration;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingLetterEvent;


@ExtendWith(MockitoExtension.class)
public class LetterProcessorTests {

    @Mock
    private RetryProperties mockRetryProperties;
    @Mock
    private Defaults mockDefaults;
    @Mock
    private Defaults.DigitalMail mockDefaultsDigitalMailSettings;
    @Mock
    private DigitalMailSenderIntegration mockDigitalMailSenderIntegration;
    @Mock
    private SnailmailSenderIntegration mockSnailmailSenderIntegration;
    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;

    private LetterProcessor letterProcessor;

    @BeforeEach
    void setUp() {
        when(mockRetryProperties.getMaxAttempts()).thenReturn(3);
        when(mockRetryProperties.getInitialDelay()).thenReturn(Duration.ofMillis(1));
        when(mockRetryProperties.getMaxDelay()).thenReturn(Duration.ofMillis(100));


        letterProcessor = new LetterProcessor(mockRetryProperties, mockMessageRepository,
                mockHistoryRepository, mockDefaults, mockDigitalMailSenderIntegration, mockSnailmailSenderIntegration);
    }

    @Test
    void testHandleIncomingMessageEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        letterProcessor.handleIncomingLetterEvent(new IncomingLetterEvent(this, "12345"));

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingLetterEvent() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");
        var letterRequest = createLetterRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(letterRequest.getParty().getPartyIds().get(0))
                        .withType(MessageType.LETTER)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(letterRequest))
                        .build()));

        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(true));

        letterProcessor.handleIncomingLetterEvent(new IncomingLetterEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(1)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingLetterEvent_couldNotSendAsDigital() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");
        var letterRequest = createLetterRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(letterRequest.getParty().getPartyIds().get(0))
                        .withType(MessageType.LETTER)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(letterRequest))
                        .build()));

        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(false));
        when(mockSnailmailSenderIntegration.sendSnailmail(any(SnailmailDto.class))).thenReturn(ResponseEntity.ok().build());

        letterProcessor.handleIncomingLetterEvent(new IncomingLetterEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockSnailmailSenderIntegration, times(1)).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }


    @Test
    void testHandleIncomingLetterEvent_couldNotSendAsDigitalOrSnail() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");
        var letterRequest = createLetterRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(letterRequest.getParty().getPartyIds().get(0))
                        .withType(MessageType.LETTER)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(letterRequest))
                        .build()));

        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(false));
        when(mockSnailmailSenderIntegration.sendSnailmail(any(SnailmailDto.class))).thenReturn(ResponseEntity.internalServerError().build());

        letterProcessor.handleIncomingLetterEvent(new IncomingLetterEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockSnailmailSenderIntegration, times(3)).sendSnailmail(any(SnailmailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingLetterEvent_couldNotSendAsDigitalAndNoSnailExists() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");
        var letterRequest = createLetterRequest(req -> req.setAttachments(List.of(LetterRequest.Attachment.builder()
                .withDeliveryMode(DIGITAL)
                .withContentType(ContentType.APPLICATION_PDF.getValue())
                .withContent("someContent")
                .withFilename("someFilename")
                .build())));
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
                MessageEntity.builder()
                        .withMessageId(messageAndDeliveryId)
                        .withDeliveryId(messageAndDeliveryId)
                        .withPartyId(letterRequest.getParty().getPartyIds().get(0))
                        .withType(MessageType.LETTER)
                        .withStatus(MessageStatus.PENDING)
                        .withContent(GSON.toJson(letterRequest))
                        .build()));

        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(false));

        letterProcessor.handleIncomingLetterEvent(new IncomingLetterEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verifyNoInteractions(mockSnailmailSenderIntegration);
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }


}

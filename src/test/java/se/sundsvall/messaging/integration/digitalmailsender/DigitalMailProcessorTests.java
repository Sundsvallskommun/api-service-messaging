package se.sundsvall.messaging.integration.digitalmailsender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createDigitalMailRequest;

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

import se.sundsvall.messaging.configuration.Defaults;
import se.sundsvall.messaging.configuration.RetryProperties;
import se.sundsvall.messaging.dto.DigitalMailDto;
import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.event.IncomingDigitalMailEvent;

@ExtendWith(MockitoExtension.class)
class DigitalMailProcessorTests {

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
    private DigitalMailSenderIntegration mockDigitalMailSenderIntegration;
    @Mock
    private Defaults mockDefaults;
    @Mock
    private Defaults.DigitalMail mockDefaultsDigitalMailSettings;
    @Mock
    private Defaults.DigitalMail.SupportInfo mockDefaultsDigitalMailSettingsSupportInfo;

    private DigitalMailProcessor digitalMailProcessor;

    @BeforeEach
    void setUp() {
        when(mockRetryProperties.getMaxAttempts()).thenReturn(3);
        when(mockRetryProperties.getInitialDelay()).thenReturn(Duration.ofMillis(1));
        when(mockRetryProperties.getMaxDelay()).thenReturn(Duration.ofMillis(100));

        digitalMailProcessor = new DigitalMailProcessor(mockRetryProperties,
            mockMessageRepository, mockHistoryRepository, mockCounterRepository,
            mockDigitalMailSenderIntegration, mockDefaults);
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenMessageIsNotFound() {
        when(mockMessageRepository.findByDeliveryId(any(String.class))).thenReturn(Optional.empty());

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, "someMessageId"));

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
        verify(mockDigitalMailSenderIntegration, never()).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, never()).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, never()).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(digitalMailRequest.getParty().getPartyIds().get(0))
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(true));

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(1)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationThrowsException() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(digitalMailRequest.getParty().getPartyIds().get(0))
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenThrow(RuntimeException.class);

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationReturnsOtherThanOk() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(digitalMailRequest.getParty().getPartyIds().get(0))
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class)))
            .thenReturn(ResponseEntity.internalServerError().build());

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void testHandleIncomingDigitalMailEvent_whenDigitalMailSenderIntegrationReturnsOkButFalse() {
        var digitalMailRequest = createDigitalMailRequest();
        var messageAndDeliveryId = UUID.randomUUID().toString();

        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");

        when(mockMessageRepository.findByDeliveryId(eq(messageAndDeliveryId))).thenReturn(Optional.of(
            MessageEntity.builder()
                .withMessageId(messageAndDeliveryId)
                .withDeliveryId(messageAndDeliveryId)
                .withPartyId(digitalMailRequest.getParty().getPartyIds().get(0))
                .withType(MessageType.DIGITAL_MAIL)
                .withStatus(MessageStatus.PENDING)
                .withContent(GSON.toJson(digitalMailRequest))
                .build()));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(ResponseEntity.ok(false));

        digitalMailProcessor.handleIncomingDigitalMailEvent(new IncomingDigitalMailEvent(this, messageAndDeliveryId));

        verify(mockMessageRepository, times(1)).findByDeliveryId(eq(messageAndDeliveryId));
        verify(mockDigitalMailSenderIntegration, times(3)).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_mapToDto() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someMunicipalityId");

        var digitalMailRequest = createDigitalMailRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(digitalMailRequest.getParty().getPartyIds().get(0))
            .withType(MessageType.DIGITAL_MAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(digitalMailRequest))
            .build();

        var dto = digitalMailProcessor.mapToDto(message);

        assertThat(dto.getSubject()).isEqualTo(digitalMailRequest.getSubject());
        assertThat(dto.getPartyId()).isEqualTo(digitalMailRequest.getParty().getPartyIds().get(0));
        assertThat(dto.getContentType()).isEqualTo(ContentType.fromString(digitalMailRequest.getContentType()));
        assertThat(dto.getBody()).isEqualTo(digitalMailRequest.getBody());
        assertThat(dto.getAttachments()).hasSameSizeAs(digitalMailRequest.getAttachments());
    }

    @Test
    void test_mapToDto_whenSenderIsMissing() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getMunicipalityId()).thenReturn("someDefaultMunicipalityId");
        when(mockDefaultsDigitalMailSettings.getSupportInfo()).thenReturn(mockDefaultsDigitalMailSettingsSupportInfo);
        when(mockDefaultsDigitalMailSettingsSupportInfo.getText()).thenReturn("someDefaultSupportInfoText");
        when(mockDefaultsDigitalMailSettingsSupportInfo.getUrl()).thenReturn("someDefaultSupportInfoUrl");
        when(mockDefaultsDigitalMailSettingsSupportInfo.getEmailAddress()).thenReturn("someDefaultSupportInfoEmailAddress");
        when(mockDefaultsDigitalMailSettingsSupportInfo.getPhoneNumber()).thenReturn("someDefaultSupportInfoPhoneNumber");

        var request = createDigitalMailRequest();

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(request.getParty().getPartyIds().get(0))
            .withType(MessageType.DIGITAL_MAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
        var dto = digitalMailProcessor.mapToDto(message);

        assertThat(dto.getSubject()).isEqualTo("someSubject");
        assertThat(dto.getSender()).satisfies(sender -> {
            assertThat(sender.getMunicipalityId()).isEqualTo("someDefaultMunicipalityId");
            assertThat(sender.getSupportInfo()).satisfies(supportInfo -> {
                assertThat(supportInfo.getText()).isEqualTo("someDefaultSupportInfoText");
                assertThat(supportInfo.getUrl()).isEqualTo("someDefaultSupportInfoUrl");
                assertThat(supportInfo.getEmailAddress()).isEqualTo("someDefaultSupportInfoEmailAddress");
                assertThat(supportInfo.getPhoneNumber()).isEqualTo("someDefaultSupportInfoPhoneNumber");
            });
        });
        assertThat(dto.getPartyId()).isEqualTo(request.getParty().getPartyIds().get(0));
        assertThat(dto.getContentType()).isEqualTo(ContentType.fromString(request.getContentType()));
        assertThat(dto.getBody()).isEqualTo(request.getBody());
        assertThat(dto.getAttachments()).hasSameSizeAs(request.getAttachments());

    }

    @Test
    void test_mapToDto_whenSubjectIsMissing() {
        when(mockDefaults.getDigitalMail()).thenReturn(mockDefaultsDigitalMailSettings);
        when(mockDefaultsDigitalMailSettings.getSubject()).thenReturn("someDefaultSubject");

        var request = createDigitalMailRequest(req -> req.setSubject(null));

        var message = MessageEntity.builder()
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(request.getParty().getPartyIds().get(0))
            .withType(MessageType.DIGITAL_MAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent(GSON.toJson(request))
            .build();
        var dto = digitalMailProcessor.mapToDto(message);

        assertThat(dto.getSubject()).isEqualTo("someDefaultSubject");
        assertThat(dto.getPartyId()).isEqualTo(request.getParty().getPartyIds().get(0));
        assertThat(dto.getContentType()).isEqualTo(ContentType.fromString(request.getContentType()));
        assertThat(dto.getBody()).isEqualTo(request.getBody());
        assertThat(dto.getAttachments()).hasSameSizeAs(request.getAttachments());
    }
}

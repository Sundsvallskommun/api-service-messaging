package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.MessageBatchDto;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.EmailRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.SmsRepository;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.model.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.model.FeedbackSettingDto;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.model.Party;

@ExtendWith(MockitoExtension.class)
class MessageServiceTests {
    
    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private FeedbackSettingsIntegration mockFeedbackSettingsIntegration;
    @Mock
    private SmsRepository mockSmsRepository;
    @Mock
    private EmailRepository mockEmailRepository;
    @Mock
    private HistoryService mockHistoryService;
    @Mock
    private DefaultSettings mockDefaultSettings;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(mockMessageRepository, mockSmsRepository,
            mockEmailRepository, mockHistoryService, mockDefaultSettings, mockFeedbackSettingsIntegration);
    }

    @Test
    void saveIncomingMessages_whenThereAreMessagesToSave_thenSaveMessages() {
        var messageBatch = MessageBatchDto.builder()
            .withBatchId(BATCH_ID)
            .withMessages(List.of(createBatchMessage(), createBatchMessage(), createBatchMessage()))
            .build();
        var numberOfMessages = messageBatch.getMessages().size();

        when(mockMessageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(anyString())).thenReturn(List.of());

        messageService.saveIncomingMessages(messageBatch);

        verify(mockMessageRepository, times(numberOfMessages)).save(any());
    }

    @Test
    void saveIncomingMessages_whenNoPartyIdSpecified_thenIgnoreMessage() {
        var nullPartyId = createBatchMessage().toBuilder()
            .withParty(Party.builder()
                .withPartyId(null)
                .build())
            .build();
        var emptyPartyId = createBatchMessage().toBuilder()
            .withParty(Party.builder()
                .withPartyId("  ")
                .build())
            .build();

        var messageBatch = MessageBatchDto.builder()
            .withBatchId(BATCH_ID)
            .withMessages(List.of(nullPartyId, emptyPartyId))
            .build();

        messageService.saveIncomingMessages(messageBatch);

        verifyNoInteractions(mockMessageRepository);
    }

    @Test
    void saveIncomingMessages_whenNoFeedbackSettingFound_thenMoveUndeliverableToHistory() {
        var messageBatch = MessageBatchDto.builder()
            .withMessages(List.of(createBatchMessage()))
            .build();

        when(mockMessageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(anyString())).thenReturn(List.of());

        messageService.saveIncomingMessages(messageBatch);

        verify(mockHistoryService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(mockMessageRepository, times(1)).deleteById(anyString());
    }
    @Test
    void saveIncomingMessages_whenIncomingIsSaved_thenMoveMessageAndDeleteFromIncoming() {
        var messageBatch = MessageBatchDto.builder()
            .withBatchId(BATCH_ID)
            .withMessages(List.of(createBatchMessage()))
            .build();
        var feedbackSettingDto = createFeedbackSetting(createFeedbackChannel(ContactMethod.EMAIL));

        when(mockMessageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(mockFeedbackSettingsIntegration.getSettingsByPartyId(anyString())).thenReturn(List.of(feedbackSettingDto));

        messageService.saveIncomingMessages(messageBatch);

        verify(mockMessageRepository, times(1)).save(any());
        verify(mockMessageRepository, times(1)).deleteById(anyString());
    }

    @Test
    void moveIncomingMessage_whenNoFeedbackWanted_thenMoveToHistoryWithStatus_NO_FEEDBACK_WANTED() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL).toBuilder()
            .withFeedbackWanted(false)
            .build();
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);
        var undeliverableCaptor = ArgumentCaptor.forClass(UndeliverableMessageDto.class);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedbackSettingDto);

        verify(mockHistoryService, times(1)).createHistory(undeliverableCaptor.capture());
        verify(mockMessageRepository, times(1)).deleteById(anyString());

        assertThat(undeliverableCaptor.getValue().getStatus()).isEqualTo(MessageStatus.NO_FEEDBACK_WANTED);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelIsForEmail_thenMoveToEmailAndDeleteIncoming() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL);
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedbackSettingDto);

        verify(mockEmailRepository, times(1)).save(any());
        verify(mockMessageRepository, times(1)).deleteById(anyString());

        verifyNoInteractions(mockSmsRepository, mockHistoryService);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelIsForSms_thenMoveToSmsAndDeleteIncoming() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.SMS);
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.SMS), feedbackSettingDto);

        verify(mockSmsRepository, times(1)).save(any());
        verify(mockMessageRepository, times(1)).deleteById(anyString());

        verifyNoInteractions(mockEmailRepository, mockHistoryService);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelHasUnknownMethod_thenMoveUndeliverableToHistory() {
        var feedbackChannel = createFeedbackChannel(null);
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedbackSettingDto);

        verify(mockHistoryService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(mockMessageRepository, times(1)).deleteById(anyString());
    }

    @Test
    void moveIncomingMessage_whenMovingEmailWithoutSenderInformation_thenUseDefaultSettings() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL);
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);

        var emailWithoutSenderName = createMessage(MessageType.EMAIL).toBuilder()
            .withEmailName(null)
            .build();
        var emailWithoutSenderEmail = createMessage(MessageType.EMAIL).toBuilder()
            .withSenderEmail(null)
            .build();

        messageService.moveIncomingMessage(emailWithoutSenderName, feedbackSettingDto);
        messageService.moveIncomingMessage(emailWithoutSenderEmail, feedbackSettingDto);

        verify(mockDefaultSettings, times(1)).getEmailName();
        verify(mockDefaultSettings, times(1)).getEmailAddress();
    }

    @Test
    void moveIncomingMessage_whenMovingSmsWithoutSenderName_thenUseDefaultSettings() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.SMS);
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);

        var smsWithoutSenderName = createMessage(MessageType.SMS).toBuilder()
            .withSmsName(null)
            .build();

        messageService.moveIncomingMessage(smsWithoutSenderName, feedbackSettingDto);

        verify(mockDefaultSettings, times(1)).getSmsName();
    }

    @Test
    void moveIncomingMessage_whenMovingSmsAndChannelDestinationIsMissingAreaCode_thenSetAreaCode() {
        var feedbackChannel = createFeedbackChannel(ContactMethod.SMS).toBuilder()
            .withDestination("0701234567")
            .build();
        var feedbackSettingDto = createFeedbackSetting(feedbackChannel);
        var smsWithoutSenderName = createMessage(MessageType.SMS);

        var smsCaptor = ArgumentCaptor.forClass(SmsEntity.class);

        messageService.moveIncomingMessage(smsWithoutSenderName, feedbackSettingDto);

        verify(mockSmsRepository, times(1)).save(smsCaptor.capture());
        
        assertThat(smsCaptor.getValue().getMobileNumber()).startsWith("+46");
    }

    private MessageEntity createMessage(MessageType type) {
        return MessageEntity.builder()
            .withBatchId(BATCH_ID)
            .withMessageId(MESSAGE_ID)
            .withPartyId(PARTY_ID)
            .withExternalReferences(Map.of("key", "value"))
            .withMessage("Message content")
            .withSubject("Message subject")
            .withEmailName("Sundsvalls kommun")
            .withSmsName("Sundsvall")
            .withSenderEmail("noreply@sundsvall.se")
            .withMessageType(type)
            .withMessageStatus(MessageStatus.PENDING)
            .build();
    }

    private FeedbackSettingDto.Channel createFeedbackChannel(ContactMethod contactMethod) {
        return FeedbackSettingDto.Channel.builder()
            .withDestination(contactMethod == ContactMethod.EMAIL ? "john.doe@example.com" : "+46701234567")
            .withContactMethod(contactMethod)
            .withFeedbackWanted(true)
            .build();
    }

    private FeedbackSettingDto createFeedbackSetting(FeedbackSettingDto.Channel channel) {
        return FeedbackSettingDto.builder()
            .withId(UUID.randomUUID().toString())
            .withPartyId(PARTY_ID)
            .withOrganizationId(UUID.randomUUID().toString())
            .withChannels(List.of(channel))
            .build();
    }

    private MessageBatchDto.Message createBatchMessage() {
        return MessageBatchDto.Message.builder()
            .withMessageId(MESSAGE_ID)
            .withParty(Party.builder()
                .withPartyId(PARTY_ID)
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .withSubject("Message subject")
            .withMessage("Message content")
            .withEmailName("Sundsvalls kommun")
            .withSmsName("Sundsvall")
            .withSenderEmail("noreply@sundsvall.se")
            .build();
    }
}

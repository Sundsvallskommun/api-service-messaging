package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.MessageType;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.feedbacksettings.ContactMethod;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingDto;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.model.dto.MessageBatchDto;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.MessageEntity;
import se.sundsvall.messaging.model.entity.SmsEntity;
import se.sundsvall.messaging.repository.EmailRepository;
import se.sundsvall.messaging.repository.MessageRepository;
import se.sundsvall.messaging.repository.SmsRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {
    
    private static final String PARTY_ID = UUID.randomUUID().toString();
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private FeedbackSettingsIntegration feedbackSettings;
    @Mock
    private SmsRepository smsRepository;
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private HistoryService historyService;
    @Mock
    private DefaultSettings defaultSettings;

    private MessageService messageService;

    @BeforeEach
    void setUp() {
        messageService = new MessageService(messageRepository, feedbackSettings, smsRepository,
                emailRepository, historyService, defaultSettings);
    }

    @Test
    void saveIncomingMessages_whenThereAreMessagesToSave_thenSaveMessages() {
        MessageBatchDto messageBatch = MessageBatchDto.builder()
                .withBatchId(BATCH_ID)
                .withMessages(List.of(createBatchMessage(), createBatchMessage(), createBatchMessage()))
                .build();
        int numberOfMessages = messageBatch.getMessages().size();

        when(messageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(feedbackSettings.getSettingsByPartyId(anyString())).thenReturn(List.of());

        messageService.saveIncomingMessages(messageBatch);

        verify(messageRepository, times(numberOfMessages)).save(any());
    }

    @Test
    void saveIncomingMessages_whenNoPartyIdSpecified_thenIgnoreMessage() {
        MessageBatchDto.Message nullPartyId = createBatchMessage().toBuilder()
                .withPartyId(null)
                .build();
        MessageBatchDto.Message emptyPartyId = createBatchMessage().toBuilder()
                .withPartyId("  ")
                .build();

        MessageBatchDto messageBatch = MessageBatchDto.builder()
                .withBatchId(BATCH_ID)
                .withMessages(List.of(nullPartyId, emptyPartyId))
                .build();

        messageService.saveIncomingMessages(messageBatch);

        verifyNoInteractions(messageRepository);
    }

    @Test
    void saveIncomingMessages_whenNoFeedbackSettingFound_thenMoveUndeliverableToHistory() {
        MessageBatchDto messageBatch = MessageBatchDto.builder()
                .withMessages(List.of(createBatchMessage()))
                .build();

        when(messageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(feedbackSettings.getSettingsByPartyId(anyString())).thenReturn(List.of());

        messageService.saveIncomingMessages(messageBatch);

        verify(historyService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(messageRepository, times(1)).deleteById(anyString());
    }
    @Test
    void saveIncomingMessages_whenIncomingIsSaved_thenMoveMessageAndDeleteFromIncoming() {
        MessageBatchDto messageBatch = MessageBatchDto.builder()
                .withBatchId(BATCH_ID)
                .withMessages(List.of(createBatchMessage()))
                .build();
        FeedbackSettingDto feedback = createFeedbackSetting(createFeedbackChannel(ContactMethod.EMAIL));

        when(messageRepository.save(any())).then(answer -> answer.getArgument(0));
        when(feedbackSettings.getSettingsByPartyId(anyString())).thenReturn(List.of(feedback));

        messageService.saveIncomingMessages(messageBatch);

        verify(messageRepository, times(1)).save(any());
        verify(messageRepository, times(1)).deleteById(anyString());
    }

    @Test
    void moveIncomingMessage_whenNoFeedbackWanted_thenMoveToHistoryWithStatus_NO_FEEDBACK_WANTED() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL).toBuilder()
                .withFeedbackWanted(false)
                .build();
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);
        ArgumentCaptor<UndeliverableMessageDto> undeliverableCaptor = ArgumentCaptor.forClass(UndeliverableMessageDto.class);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedback);

        verify(historyService, times(1)).createHistory(undeliverableCaptor.capture());
        verify(messageRepository, times(1)).deleteById(anyString());

        assertThat(undeliverableCaptor.getValue().getStatus()).isEqualTo(MessageStatus.NO_FEEDBACK_WANTED);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelIsForEmail_thenMoveToEmailAndDeleteIncoming() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL);
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedback);

        verify(emailRepository, times(1)).save(any());
        verify(messageRepository, times(1)).deleteById(anyString());

        verifyNoInteractions(smsRepository, historyService);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelIsForSms_thenMoveToSmsAndDeleteIncoming() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.SMS);
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.SMS), feedback);

        verify(smsRepository, times(1)).save(any());
        verify(messageRepository, times(1)).deleteById(anyString());

        verifyNoInteractions(emailRepository, historyService);
    }

    @Test
    void moveIncomingMessage_whenFeedbackChannelHasUnknownMethod_thenMoveUndeliverableToHistory() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(null);
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);

        messageService.moveIncomingMessage(createMessage(MessageType.EMAIL), feedback);

        verify(historyService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(messageRepository, times(1)).deleteById(anyString());
    }

    @Test
    void moveIncomingMessage_whenMovingEmailWithoutSenderInformation_thenUseDefaultSettings() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.EMAIL);
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);

        MessageEntity emailWithoutSenderName = createMessage(MessageType.EMAIL).toBuilder()
                .withEmailName(null)
                .build();
        MessageEntity emailWithoutSenderEmail = createMessage(MessageType.EMAIL).toBuilder()
                .withSenderEmail(null)
                .build();

        messageService.moveIncomingMessage(emailWithoutSenderName, feedback);
        messageService.moveIncomingMessage(emailWithoutSenderEmail, feedback);

        verify(defaultSettings, times(1)).getEmailName();
        verify(defaultSettings, times(1)).getEmailAddress();
    }

    @Test
    void moveIncomingMessage_whenMovingSmsWithoutSenderName_thenUseDefaultSettings() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.SMS);
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);

        MessageEntity smsWithoutSenderName = createMessage(MessageType.SMS).toBuilder()
                .withSmsName(null)
                .build();

        messageService.moveIncomingMessage(smsWithoutSenderName, feedback);

        verify(defaultSettings, times(1)).getSmsName();
    }

    @Test
    void moveIncomingMessage_whenMovingSmsAndChannelDestinationIsMissingAreaCode_thenSetAreaCode() {
        FeedbackSettingDto.Channel feedbackChannel = createFeedbackChannel(ContactMethod.SMS).toBuilder()
                .withDestination("0701234567")
                .build();
        FeedbackSettingDto feedback = createFeedbackSetting(feedbackChannel);
        MessageEntity smsWithoutSenderName = createMessage(MessageType.SMS);

        ArgumentCaptor<SmsEntity> smsCaptor = ArgumentCaptor.forClass(SmsEntity.class);

        messageService.moveIncomingMessage(smsWithoutSenderName, feedback);

        verify(smsRepository, times(1)).save(smsCaptor.capture());
        
        assertThat(smsCaptor.getValue().getMobileNumber()).startsWith("+46");
    }

    private MessageEntity createMessage(MessageType type) {
        return MessageEntity.builder()
                .withBatchId(BATCH_ID)
                .withMessageId(MESSAGE_ID)
                .withPartyId(PARTY_ID)
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
                .withPartyId(PARTY_ID)
                .withSubject("Message subject")
                .withMessage("Message content")
                .withEmailName("Sundsvalls kommun")
                .withSmsName("Sundsvall")
                .withSenderEmail("noreply@sundsvall.se")
                .build();
    }
}

package se.sundsvall.messaging.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidLetterRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.LetterRequest;
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.api.model.request.SlackRequest;
import se.sundsvall.messaging.api.model.request.SmsRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.contactsettings.ContactDto;
import se.sundsvall.messaging.integration.contactsettings.ContactSettingsIntegration;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailDto;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailDto;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.slack.SlackDto;
import se.sundsvall.messaging.integration.slack.SlackIntegration;
import se.sundsvall.messaging.integration.smssender.SmsDto;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailDto;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageDto;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.mapper.DtoMapper;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageServiceTests {

    @Mock
    private TransactionTemplate mockTransactionTemplate;
    @Mock
    private BlacklistService mockBlacklistService;
    @Mock
    private DbIntegration mockDbIntegration;
    @Mock
    private ContactSettingsIntegration mockContactSettingsIntegration;
    @Mock
    private SmsSenderIntegration mockSmsSenderIntegration;
    @Mock
    private EmailSenderIntegration mockEmailSenderIntegration;
    @Mock
    private DigitalMailSenderIntegration mockDigitalMailSenderIntegration;
    @Mock
    private WebMessageSenderIntegration mockWebMessageSenderIntegration;
    @Mock
    private SnailMailSenderIntegration mockSnailMailSenderIntegration;
    @Mock
    private SlackIntegration mockSlackIntegration;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private MessageMapper mockMessageMapper;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private RequestMapper mockRequestMapper;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private DtoMapper mockDtoMapper;

    private List<Object> integrations;

    @InjectMocks
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        integrations = List.of(
            mockContactSettingsIntegration,
            mockEmailSenderIntegration,
            mockSmsSenderIntegration,
            mockDigitalMailSenderIntegration,
            mockWebMessageSenderIntegration,
            mockSnailMailSenderIntegration,
            mockSlackIntegration
        );

        when(mockTransactionTemplate.execute(any(TransactionCallbackWithoutResult.class)))
            .then(invocationOnMock -> {
                var args = invocationOnMock.getArguments();
                var arg = (TransactionCallbackWithoutResult) args[0];

                return arg.doInTransaction(new SimpleTransactionStatus());
            });
    }

    @Test
    void test_sendSms() {
        var request = createValidSmsRequest();
        var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(SENT);

        var result = messageService.sendSms(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(SMS);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockSmsSenderIntegration, times(1)).sendSms(any(SmsDto.class));
        verifyNoMoreInteractions(mockSmsSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockSmsSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(SmsRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toSmsDto(any(SmsRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendEmail() {
        var request = createValidEmailRequest();
        var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(SENT);

        var result = messageService.sendEmail(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(EMAIL);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockEmailSenderIntegration, times(1)).sendEmail(any(EmailDto.class));
        verifyNoMoreInteractions(mockEmailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockEmailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(EmailRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toEmailDto(any(EmailRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendWebMessage() {
        var request = createValidWebMessageRequest();
        var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class))).thenReturn(SENT);

        var result = messageService.sendWebMessage(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(WEB_MESSAGE);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockWebMessageSenderIntegration, times(1)).sendWebMessage(any(WebMessageDto.class));
        verifyNoMoreInteractions(mockWebMessageSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockWebMessageSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(WebMessageRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toWebMessageDto(any(WebMessageRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendSnailMail() {
        var request = createValidSnailMailRequest();
        var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockSnailMailSenderIntegration.sendSnailMail(any(SnailMailDto.class))).thenReturn(SENT);

        var result = messageService.sendSnailMail(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockSnailMailSenderIntegration, times(1)).sendSnailMail(any(SnailMailDto.class));
        verifyNoMoreInteractions(mockSnailMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockSnailMailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(SnailMailRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toSnailmailDto(any(SnailMailRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendToSlack() {
        var request = createValidSlackRequest();
        var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockSlackIntegration.sendMessage(any(SlackDto.class))).thenReturn(SENT);

        var result = messageService.sendToSlack(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(SLACK);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockSlackIntegration, times(1)).sendMessage(any(SlackDto.class));
        verifyNoMoreInteractions(mockSlackIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockSlackIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(SlackRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toSlackDto(any(SlackRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendDigitalMail() {
        var request = createValidDigitalMailRequest();
        var messages = mockMessageMapper.toMessages(request, "someBatchId");

        when(mockDbIntegration.saveMessages(ArgumentMatchers.anyList())).thenReturn(messages);
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(SENT);

        var result = messageService.sendDigitalMail(request);

        assertThat(result.batchId()).isNotNull();   // TODO: assert UUID
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().get(0).messageId()).isValidUuid().isEqualTo(messages.get(0).messageId());
        assertThat(result.deliveries().get(0).deliveryId()).isValidUuid().isEqualTo(messages.get(0).deliveryId());
        assertThat(result.deliveries().get(0).messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().get(0).status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration, times(1)).sendDigitalMail(any(DigitalMailDto.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockDigitalMailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verify(mockTransactionTemplate, times(1)).execute(any(TransactionCallbackWithoutResult.class));
    }

    @Test
    void test_sendLetter() {
        var request = createValidLetterRequest();
        var messages = mockMessageMapper.toMessages(request, "someBatchId");

        when(mockDbIntegration.saveMessages(ArgumentMatchers.anyList())).thenReturn(messages);
        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(SENT);

        var result = messageService.sendLetter(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().get(0).messageId()).isValidUuid();
        assertThat(result.deliveries().get(0).deliveryId()).isValidUuid();
        assertThat(result.deliveries().get(0).messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().get(0).status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration, times(1)).sendDigitalMail(any(DigitalMailDto.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockDigitalMailSenderIntegration);
        // Verify db integration interactions
        verify(mockDbIntegration, times(1)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(1)).deleteMessageByDeliveryId(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verify(mockRequestMapper, times(1)).toDigitalMailRequest(any(LetterRequest.class), any(String.class));
        verify(mockRequestMapper, times(1)).toSnailMailRequest(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void test_sendMessages() {
        var request = createMessageRequest(List.of("partyId1", "partyId2", "partyId3", "partyId4"));

        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockContactSettingsIntegration.getContactSettings(eq("partyId1"), any()))
            .thenReturn(List.of(
                ContactDto.builder()
                    .withContactMethod(ContactDto.ContactMethod.SMS)
                    .withDestination("+46701234567")
                    .withDisabled(false)
                    .build(),
                ContactDto.builder()
                    .withContactMethod(ContactDto.ContactMethod.EMAIL)
                    .withDestination("partyId1@something.com")
                    .withDisabled(true)
                    .build()));
        when(mockContactSettingsIntegration.getContactSettings(eq("partyId2"), any()))
            .thenReturn(List.of(ContactDto.builder().build()));
        when(mockContactSettingsIntegration.getContactSettings(eq("partyId3"), any()))
            .thenReturn(List.of(
                ContactDto.builder()
                    .withContactMethod(ContactDto.ContactMethod.EMAIL)
                    .withDestination("partyId3@something.com")
                    .withDisabled(false)
                    .build()));
        when(mockContactSettingsIntegration.getContactSettings(eq("partyId4"), any()))
            .thenReturn(List.of());

        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(SENT);
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(FAILED);

        var result = messageService.sendMessages(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(5);
        assertThat(result.deliveries()).extracting(InternalDeliveryResult::status)
            .containsExactlyInAnyOrder(SENT, FAILED, FAILED, NO_CONTACT_WANTED, NO_CONTACT_SETTINGS_FOUND);
        assertThat(result.deliveries()).extracting(InternalDeliveryResult::messageType)
            .containsExactlyInAnyOrder(SMS, EMAIL, MESSAGE, MESSAGE, MESSAGE);
        assertThat(result.deliveries()).allSatisfy(deliveryResult -> {
            assertThat(deliveryResult.messageId()).isValidUuid();
            assertThat(deliveryResult.deliveryId()).isValidUuid();
        });

        // Verify external integration interactions
        verify(mockContactSettingsIntegration, times(4)).getContactSettings(any(String.class), any());
        verifyNoMoreInteractions(mockContactSettingsIntegration);
        verify(mockSmsSenderIntegration, times(1)).sendSms(any(SmsDto.class));
        verifyNoMoreInteractions(mockSmsSenderIntegration);
        verify(mockEmailSenderIntegration, times(1)).sendEmail(any(EmailDto.class));
        verifyNoMoreInteractions(mockEmailSenderIntegration);
        verify(mockDbIntegration, times(5)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(7)).deleteMessageByDeliveryId(any(String.class));
        // Verify mapper interactions (4 instead of 3 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(4)).toMessage(any(String.class), any(MessageRequest.Message.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper, times(1)).toSmsDto(any(SmsRequest.class));
        verify(mockDtoMapper, times(1)).toEmailDto(any(EmailRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verify(mockRequestMapper, times(1)).toSmsRequest(any(Message.class), any(String.class));
        verify(mockRequestMapper, times(1)).toEmailRequest(any(Message.class), any(String.class));
        verifyNoMoreInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions(5);
    }

    // TODO: message x 3 (at least)

    private void verifyNoExternalIntegrationInteractionsExcept(final Object skipIntegration) {
        integrations.stream()
            .filter(integration -> !integration.equals(skipIntegration))
            .forEach(Mockito::verifyNoInteractions);
    }

    private void verifyDbIntegrationInteractions() {
        verify(mockDbIntegration, times(1)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(1)).deleteMessageByDeliveryId(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
    }

    private void verifyTransactionTemplateInteractions() {
        verifyTransactionTemplateInteractions(1);
    }

    private void verifyTransactionTemplateInteractions(int times) {
        verify(mockTransactionTemplate, times(times)).execute(any(TransactionCallbackWithoutResult.class));
        verifyNoMoreInteractions(mockTransactionTemplate);
    }

    private MessageRequest createMessageRequest(final List<String> partyIds) {
        return MessageRequest.builder()
            .withMessages(partyIds.stream().map(this::createMessage).toList())
            .build();
    }

    private MessageRequest.Message createMessage(final String partyId) {
        return MessageRequest.Message.builder()
            .withParty(MessageRequest.Message.Party.builder()
                .withPartyId(partyId)
                .withExternalReferences(List.of(createExternalReference()))
                .build())
            .withSubject("someSubject")
            .withMessage("someMessage")
            .build();
    }
}

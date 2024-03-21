package se.sundsvall.messaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
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
import se.sundsvall.messaging.integration.digitalmailsender.DigitalInvoiceDto;
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
import se.sundsvall.messaging.model.ContentType;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.service.mapper.DtoMapper;
import se.sundsvall.messaging.service.mapper.MessageMapper;
import se.sundsvall.messaging.service.mapper.RequestMapper;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalInvoiceRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidLetterRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.NOT_SENT;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_INVOICE;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.test.assertj.Assertions.assertThat;

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

    private static final String BATCH_ID = UUID.randomUUID().toString();

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
    void sendSms() {
        final var request = createValidSmsRequest();
        final var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockSmsSenderIntegration.sendSms(any(SmsDto.class))).thenReturn(SENT);

        final var result = messageService.sendSms(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(SMS);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockSmsSenderIntegration).sendSms(any(SmsDto.class));
        verifyNoMoreInteractions(mockSmsSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockSmsSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(SmsRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toSmsDto(any(SmsRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendEmail() {
        final var request = createValidEmailRequest();
        final var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockEmailSenderIntegration.sendEmail(any(EmailDto.class))).thenReturn(SENT);

        final var result = messageService.sendEmail(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(EMAIL);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockEmailSenderIntegration).sendEmail(any(EmailDto.class));
        verifyNoMoreInteractions(mockEmailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockEmailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(EmailRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toEmailDto(any(EmailRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendWebMessage() {
        final var request = createValidWebMessageRequest();
        final var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockWebMessageSenderIntegration.sendWebMessage(any(WebMessageDto.class))).thenReturn(SENT);

        final var result = messageService.sendWebMessage(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(WEB_MESSAGE);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockWebMessageSenderIntegration).sendWebMessage(any(WebMessageDto.class));
        verifyNoMoreInteractions(mockWebMessageSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockWebMessageSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(WebMessageRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toWebMessageDto(any(WebMessageRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendToSlack() {
        final var request = createValidSlackRequest();
        final var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockSlackIntegration.sendMessage(any(SlackDto.class))).thenReturn(SENT);

        final var result = messageService.sendToSlack(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(SLACK);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockSlackIntegration).sendMessage(any(SlackDto.class));
        verifyNoMoreInteractions(mockSlackIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockSlackIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(SlackRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toSlackDto(any(SlackRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendDigitalMail() {
        final var request = createValidDigitalMailRequest();
        final var messages = mockMessageMapper.toMessages(request, "someBatchId");

        when(mockDbIntegration.saveMessages(anyList())).thenReturn(messages);
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(SENT);

        var result = messageService.sendDigitalMail(request);

        assertThat(result.batchId()).isNotNull();
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().getFirst().messageId()).isValidUuid().isEqualTo(messages.getFirst().messageId());
        assertThat(result.deliveries().getFirst().deliveryId()).isValidUuid().isEqualTo(messages.getFirst().deliveryId());
        assertThat(result.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().getFirst().status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration).sendDigitalMail(any(DigitalMailDto.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockDigitalMailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verify(mockTransactionTemplate).execute(any(TransactionCallbackWithoutResult.class));
    }

    @Test
    void sendDigitalInvoice() {
        final var request = createValidDigitalInvoiceRequest();
        final var message = mockMessageMapper.toMessage(request);

        when(mockDbIntegration.saveMessage(any(Message.class))).thenReturn(message);
        when(mockDigitalMailSenderIntegration.sendDigitalInvoice(any(DigitalInvoiceDto.class))).thenReturn(SENT);

        final var result = messageService.sendDigitalInvoice(request);

        assertThat(result.messageId()).isValidUuid().isEqualTo(message.messageId());
        assertThat(result.deliveryId()).isValidUuid().isEqualTo(message.deliveryId());
        assertThat(result.messageType()).isEqualTo(DIGITAL_INVOICE);
        assertThat(result.status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration).sendDigitalInvoice(any(DigitalInvoiceDto.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockDigitalMailSenderIntegration);
        // Verify db integration interactions
        verifyDbIntegrationInteractions();
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessage(any(DigitalInvoiceRequest.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toDigitalInvoiceDto(any(DigitalInvoiceRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verifyNoInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verify(mockTransactionTemplate).execute(any(TransactionCallbackWithoutResult.class));
    }

    @Test
    void sendLetterDigital() {
        final var request = createValidLetterRequest();
        final var messages = mockMessageMapper.toMessages(request, BATCH_ID);

        when(mockDbIntegration.saveMessages(anyList())).thenReturn(messages);
        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(SENT);

        final var result = messageService.sendLetter(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().getFirst().messageId()).isValidUuid();
        assertThat(result.deliveries().getFirst().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().getFirst().status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration).sendDigitalMail(any(DigitalMailDto.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration);
        verifyNoExternalIntegrationInteractionsExcept(mockDigitalMailSenderIntegration);
        // Verify db integration interactions
        verify(mockDbIntegration).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration).deleteMessageByDeliveryId(any(String.class));
        verifyNoMoreInteractions(mockDbIntegration);
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verify(mockRequestMapper).toDigitalMailRequest(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockRequestMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendLetterSnailMailWhenDigitalNotSent() {
        final var request = createValidLetterRequest();
        final var messages = mockMessageMapper.toMessages(request, BATCH_ID);

        when(mockDbIntegration.saveMessages(anyList())).thenReturn(messages);
        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenReturn(NOT_SENT);
        when(mockSnailMailSenderIntegration.sendSnailMail(any())).thenReturn(SENT);

        final var result = messageService.sendLetter(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(2);
        assertThat(result.deliveries().getFirst().messageId()).isValidUuid();
        assertThat(result.deliveries().getFirst().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().getFirst().status()).isEqualTo(NOT_SENT);
        assertThat(result.deliveries().getLast().messageId()).isValidUuid();
        assertThat(result.deliveries().getLast().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getLast().messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(result.deliveries().getLast().status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockSnailMailSenderIntegration).sendSnailMail(any(SnailMailDto.class));
        verify(mockSnailMailSenderIntegration).sendBatch(anyString());
        // Verify db integration interactions
        verify(mockDbIntegration, times(2)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(2)).deleteMessageByDeliveryId(any(String.class));
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockDtoMapper).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verify(mockRequestMapper).toSnailMailRequest(any(LetterRequest.class), any(String.class));
        verify(mockDtoMapper).toSnailMailDto(any(SnailMailRequest.class), any(String.class));
        verify(mockRequestMapper).toDigitalMailRequest(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration, mockSnailMailSenderIntegration, mockRequestMapper, mockDtoMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions(2);
    }

    @Test
    void sendLetterSnailMailWhenExceptionSendingDigital() {
        final var request = createValidLetterRequest();
        final var messages = mockMessageMapper.toMessages(request, BATCH_ID);

        when(mockDbIntegration.saveMessages(anyList())).thenReturn(messages);
        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockDigitalMailSenderIntegration.sendDigitalMail(any(DigitalMailDto.class))).thenThrow(new RuntimeException());
        when(mockSnailMailSenderIntegration.sendSnailMail(any())).thenReturn(SENT);
        when(mockSnailMailSenderIntegration.sendSnailMail(any())).thenReturn(SENT);

        final var result = messageService.sendLetter(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(2);
        assertThat(result.deliveries().getFirst().messageId()).isValidUuid();
        assertThat(result.deliveries().getFirst().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_MAIL);
        assertThat(result.deliveries().getFirst().status()).isEqualTo(FAILED);
        assertThat(result.deliveries().getLast().messageId()).isValidUuid();
        assertThat(result.deliveries().getLast().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getLast().messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(result.deliveries().getLast().status()).isEqualTo(SENT);

        // Verify external integration interactions
        verify(mockDigitalMailSenderIntegration).sendDigitalMail(any(DigitalMailDto.class));
        verify(mockSnailMailSenderIntegration).sendSnailMail(any(SnailMailDto.class));
        verify(mockSnailMailSenderIntegration).sendBatch(anyString());
        // Verify db integration interactions
        verify(mockDbIntegration, times(2)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(2)).deleteMessageByDeliveryId(any(String.class));
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockDtoMapper).toDigitalMailDto(any(DigitalMailRequest.class), any(String.class));
        verify(mockRequestMapper).toSnailMailRequest(any(LetterRequest.class), any(String.class));
        verify(mockDtoMapper).toSnailMailDto(any(SnailMailRequest.class), any(String.class));
        verify(mockRequestMapper).toDigitalMailRequest(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration, mockSnailMailSenderIntegration, mockRequestMapper, mockDtoMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions(2);
    }

    @Test
    void sendLetterSnailMailWhenExceptionSendingSnailMail() {
        final var request = createValidLetterRequest().withAttachments(List.of( LetterRequest.Attachment.builder()
            .withDeliveryMode(LetterRequest.Attachment.DeliveryMode.SNAIL_MAIL)
            .withContentType(ContentType.APPLICATION_PDF.getValue())
            .withFilename("someFilename")
            .withContent("someContent")
            .build()));
        final var messages = mockMessageMapper.toMessages(request, BATCH_ID);

        when(mockDbIntegration.saveMessages(anyList())).thenReturn(messages);
        when(mockDbIntegration.saveMessage(any(Message.class))).thenAnswer(i -> i.getArgument(0, Message.class));
        when(mockSnailMailSenderIntegration.sendSnailMail(any())).thenThrow(new RuntimeException());

        final var result = messageService.sendLetter(request);

        assertThat(result.batchId()).isValidUuid();
        assertThat(result.deliveries()).hasSize(1);
        assertThat(result.deliveries().getFirst().messageId()).isValidUuid();
        assertThat(result.deliveries().getFirst().deliveryId()).isValidUuid();
        assertThat(result.deliveries().getFirst().messageType()).isEqualTo(SNAIL_MAIL);
        assertThat(result.deliveries().getFirst().status()).isEqualTo(FAILED);

        // Verify external integration interactions
        verify(mockSnailMailSenderIntegration).sendSnailMail(any(SnailMailDto.class));
        // Verify db integration interactions
        verify(mockDbIntegration).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration).deleteMessageByDeliveryId(any(String.class));
        // Verify mapper interactions (1 + 1 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(1 + 1)).toMessages(any(LetterRequest.class), any(String.class));
        verify(mockRequestMapper).toSnailMailRequest(any(LetterRequest.class), any(String.class));
        verify(mockDtoMapper).toSnailMailDto(any(SnailMailRequest.class), any(String.class));
        verify(mockRequestMapper).toDigitalMailRequest(any(LetterRequest.class), any(String.class));
        verifyNoMoreInteractions(mockDigitalMailSenderIntegration, mockSnailMailSenderIntegration, mockRequestMapper, mockDtoMapper);
        // Verify transaction template interaction
        verifyTransactionTemplateInteractions();
    }

    @Test
    void sendMessages() {
        final var request = createMessageRequest(List.of("partyId1", "partyId2", "partyId3", "partyId4"));

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

        final var result = messageService.sendMessages(request);

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
        verify(mockSmsSenderIntegration).sendSms(any(SmsDto.class));
        verifyNoMoreInteractions(mockSmsSenderIntegration);
        verify(mockEmailSenderIntegration).sendEmail(any(EmailDto.class));
        verifyNoMoreInteractions(mockEmailSenderIntegration);
        verify(mockDbIntegration, times(5)).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration, times(7)).deleteMessageByDeliveryId(any(String.class));
        // Verify mapper interactions (4 instead of 3 on mockMessageMapper since one is in the actual test)
        verify(mockMessageMapper, times(4)).toMessage(anyString(), anyString(), any(MessageRequest.Message.class));
        verifyNoMoreInteractions(mockMessageMapper);
        verify(mockDtoMapper).toSmsDto(any(SmsRequest.class));
        verify(mockDtoMapper).toEmailDto(any(EmailRequest.class));
        verifyNoMoreInteractions(mockDtoMapper);
        verify(mockRequestMapper).toSmsRequest(any(Message.class), any(String.class));
        verify(mockRequestMapper).toEmailRequest(any(Message.class), any(String.class));
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
        verify(mockDbIntegration).saveHistory(any(Message.class), nullable(String.class));
        verify(mockDbIntegration).deleteMessageByDeliveryId(any(String.class));
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
            .withOrigin("someOrigin")
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

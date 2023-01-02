package se.sundsvall.messaging.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.digitalmailsender.DigitalMailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.feedbacksettings.FeedbackSettingsIntegration;
import se.sundsvall.messaging.integration.smssender.SmsSenderIntegration;
import se.sundsvall.messaging.integration.snailmailsender.SnailMailSenderIntegration;
import se.sundsvall.messaging.integration.webmessagesender.WebMessageSenderIntegration;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageServiceTests {

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;
    @Mock
    private FeedbackSettingsIntegration mockFeedbackSettings;
    @Mock
    private SmsSenderIntegration mockSmsSender;
    @Mock
    private EmailSenderIntegration mockEmailSender;
    @Mock
    private DigitalMailSenderIntegration mockDigitalMailSender;
    @Mock
    private WebMessageSenderIntegration mockWebMessageSender;
    @Mock
    private SnailMailSenderIntegration mockSnailmailSender;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private MessageMapper mockMapper;

    @InjectMocks
    private MessageService messageService;

    // TODO
}

package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import se.sundsvall.messaging.api.model.EmailRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.EmailRepository;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegration;
import se.sundsvall.messaging.integration.emailsender.EmailSenderIntegrationProperties;
import se.sundsvall.messaging.model.ExternalReference;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.Party;

@ExtendWith(MockitoExtension.class)
class EmailServiceTests {
    
    @Mock
    private EmailRepository mockEmailRepository;
    @Mock
    private DefaultSettings mockDefaultSettings;
    @Mock
    private HistoryService mockHistoryService;
    @Mock
    private EmailSenderIntegration mockEmailSenderIntegration;
    @Mock
    private EmailSenderIntegrationProperties mockEmailSenderIntegrationProperties;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mockDefaultSettings, mockEmailRepository,
            mockEmailSenderIntegrationProperties, mockEmailSenderIntegration, mockHistoryService);
    }

    @Test
    void test_getPollDelay() {
        var pollDelay = Duration.ofSeconds(12);

        when(mockEmailSenderIntegrationProperties.getPollDelay()).thenReturn(pollDelay);

        assertThat(emailService.getPollDelay()).isEqualTo(pollDelay);

        verify(mockEmailSenderIntegrationProperties, times(1)).getPollDelay();
    }

    @Test
    void test_run() {
        assertThatNoException().isThrownBy(() -> emailService.run());
    }

    @Test
    void saveEmail_givenIncomingEmail_thenSaveEmail() {
        var email = createEmailRequest();

        emailService.saveEmail(email);

        verify(mockEmailRepository, times(1)).save(any());
    }

    @Test
    void saveEmail_givenIncomingEmailWithNoSenderEmailOrName_thenUseDefaultSettings() {
        var emailWithNullSenderName = createEmailRequest();
        var emailWithBlankSenderName = createEmailRequest();
        var emailWithNullSenderEmail = createEmailRequest();
        var emailWithBlankSenderEmail = createEmailRequest();

        emailWithNullSenderName.setSenderName(null);
        emailWithBlankSenderName.setSenderName("");

        emailWithNullSenderEmail.setSenderEmail(null);
        emailWithBlankSenderEmail.setSenderEmail("");

        emailService.saveEmail(emailWithNullSenderName);
        emailService.saveEmail(emailWithBlankSenderName);
        emailService.saveEmail(emailWithNullSenderEmail);
        emailService.saveEmail(emailWithBlankSenderEmail);

        verify(mockDefaultSettings, times(2)).getEmailName();
        verify(mockDefaultSettings, times(2)).getEmailAddress();
    }

    @Test
    void sendOldestPendingEmail_whenEmailSentWithResponseStatus_OK_thenMoveToHistory() {
        var emailEntities = List.of(createEmail(null));

        when(mockEmailRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(emailEntities);
        when(mockEmailSenderIntegrationProperties.getMaxRetries()).thenReturn(3);
        when(mockEmailSenderIntegration.sendEmail(any())).thenReturn(HttpStatus.OK);

        emailService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(EmailEntity.class));
    }

    @Test
    void sendOldestPendingEmail_whenNoPendingEmails_thenDoNothing() {
        when(mockEmailRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(List.of());

        emailService.sendOldestPendingMessages();

        verifyNoInteractions(mockEmailSenderIntegration, mockHistoryService);
    }

    @Test
    void sendOldestPendingEmail_whenEmailExceededMaxSendingAttempts_thenMoveToHistoryAsUndeliverable() {
        var emails = List.of(createEmail(message -> message.setSendingAttempts(3)));

        when(mockEmailRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(emails);
        when(mockEmailSenderIntegrationProperties.getMaxRetries()).thenReturn(3);

        emailService.sendOldestPendingMessages();

        verify(mockHistoryService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(mockEmailRepository, times(1)).deleteById(anyString());
    }

    @Test
    void sendOldestPendingEmail_whenAttemptingToSendEmailAndFail_thenUpdateSendingAttempts() {
        var emailEntities = List.of(createEmail(message -> message.setSendingAttempts(0)));
        var emailCaptor = ArgumentCaptor.forClass(EmailEntity.class);

        when(mockEmailRepository.findLatestWithStatus(any(MessageStatus.class))).thenReturn(emailEntities);
        when(mockEmailSenderIntegrationProperties.getMaxRetries()).thenReturn(3);
        when(mockEmailSenderIntegration.sendEmail(any())).thenReturn(HttpStatus.BAD_GATEWAY);

        emailService.sendOldestPendingMessages();

        verify(mockEmailRepository, times(1)).save(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getSendingAttempts()).isEqualTo(1);
    }

    private EmailEntity createEmail(Consumer<EmailEntity> modifier) {
        var emailEntity = EmailEntity.builder()
            .withCreatedAt(LocalDateTime.now())
            .withStatus(MessageStatus.PENDING)
            .withSenderEmail("test@hotmail.com")
            .withSenderName("name")
            .withBatchId(UUID.randomUUID().toString())
            .withHtmlMessage("html message")
            .withMessage("message")
            .withSubject("subject")
            .withPartyId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withSendingAttempts(0)
            .withAttachments(List.of(EmailEntity.Attachment.builder()
                    .withContent("content")
                    .withContentType("contentType")
                    .withName("name")
                    .build()))
            .build();

        if (modifier != null) {
            modifier.accept(emailEntity);
        }

        return emailEntity;
    }

    private EmailRequest createEmailRequest() {
        return EmailRequest.builder()
            .withSenderEmail("test@hotmail.com")
            .withSenderName("test name")
            .withMessage("test message")
            .withSubject("subject")
            .withParty(Party.builder()
                .withPartyId("party id")
                .withExternalReferences(List.of(
                    ExternalReference.builder()
                        .withKey("key")
                        .withValue("value")
                        .build()))
                .build())
            .withEmailAddress("test2@hotmail.com")
            .withHtmlMessage("test")
            .withAttachments(List.of(EmailRequest.Attachment.builder()
                    .withContent("content")
                    .withContentType("content type")
                    .withName("name")
                    .build()))
            .build();
    }
}

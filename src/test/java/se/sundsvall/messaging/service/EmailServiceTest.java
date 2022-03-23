package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
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

import se.sundsvall.messaging.api.MessageStatus;
import se.sundsvall.messaging.api.request.IncomingEmailRequest;
import se.sundsvall.messaging.configuration.DefaultSettings;
import se.sundsvall.messaging.integration.email.EmailIntegration;
import se.sundsvall.messaging.model.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.model.entity.EmailEntity;
import se.sundsvall.messaging.repository.EmailRepository;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {
    
    @Mock
    private EmailRepository emailRepository;
    @Mock
    private DefaultSettings defaultSettings;
    @Mock
    private HistoryService historyService;
    @Mock
    private EmailIntegration emailIntegration;
    private EmailService emailService;

    @BeforeEach
    void setUp(){
        emailService = new EmailService(emailRepository, defaultSettings, emailIntegration, historyService);
    }

    @Test
    void saveEmail_givenIncomingEmail_thenSaveEmail() {
        IncomingEmailRequest email = createEmailRequest();

        emailService.saveEmail(email);

        verify(emailRepository, times(1)).save(any());
    }

    @Test
    void saveEmail_givenIncomingEmailWithNoSenderEmailOrName_thenUseDefaultSettings() {
        IncomingEmailRequest emailWithNullSenderName = createEmailRequest();
        IncomingEmailRequest emailWithBlankSenderName = createEmailRequest();
        IncomingEmailRequest emailWithNullSenderEmail = createEmailRequest();
        IncomingEmailRequest emailWithBlankSenderEmail = createEmailRequest();

        emailWithNullSenderName.setSenderName(null);
        emailWithBlankSenderName.setSenderName("");

        emailWithNullSenderEmail.setSenderEmail(null);
        emailWithBlankSenderEmail.setSenderEmail("");


        emailService.saveEmail(emailWithNullSenderName);
        emailService.saveEmail(emailWithBlankSenderName);
        emailService.saveEmail(emailWithNullSenderEmail);
        emailService.saveEmail(emailWithBlankSenderEmail);

        verify(defaultSettings, times(2)).getEmailName();
        verify(defaultSettings, times(2)).getEmailAddress();


    }

    @Test
    void sendOldestPendingEmail_whenEmailSentWithResponseStatus_OK_thenMoveToHistory() {
        List<EmailEntity> emails = List.of(createEmail(null));

        when(emailRepository.findByStatusEquals(any(), any())).thenReturn(emails);
        when(emailIntegration.getMessageRetries()).thenReturn(3);
        when(emailIntegration.sendEmail(any())).thenReturn(HttpStatus.OK);

        emailService.sendOldestPendingEmail();

        verify(historyService, times(1)).createHistory(any(EmailEntity.class));

    }

    @Test
    void sendOldestPendingEmail_whenNoPendingEmails_thenDoNothing() {
        List<EmailEntity> emails = Collections.emptyList();

        when(emailRepository.findByStatusEquals(any(), any())).thenReturn(emails);

        emailService.sendOldestPendingEmail();

        verifyNoInteractions(emailIntegration, historyService);

    }

    @Test
    void sendOldestPendingEmail_whenEmailExceededMaxSendingAttempts_thenMoveToHistoryAsUndeliverable() {
        List<EmailEntity> emails = List.of(createEmail(message -> message.setSendingAttempts(3)));

        when(emailRepository.findByStatusEquals(any(), any())).thenReturn(emails);
        when(emailIntegration.getMessageRetries()).thenReturn(3);

        emailService.sendOldestPendingEmail();

        verify(historyService, times(1)).createHistory(any(UndeliverableMessageDto.class));
        verify(emailRepository, times(1)).deleteById(anyString());
    }

    @Test
    void sendOldestPendingEmail_whenAttemptingToSendEmailAndFail_thenUpdateSendingAttempts() {
        List<EmailEntity> email = List.of(createEmail(message -> message.setSendingAttempts(0)));
        ArgumentCaptor<EmailEntity> emailCaptor = ArgumentCaptor.forClass(EmailEntity.class);

        when(emailRepository.findByStatusEquals(any(), any())).thenReturn(email);
        when(emailIntegration.getMessageRetries()).thenReturn(3);
        when(emailIntegration.sendEmail(any())).thenReturn(HttpStatus.BAD_GATEWAY);

        emailService.sendOldestPendingEmail();

        verify(emailRepository, times(1)).save(emailCaptor.capture());

        assertThat(emailCaptor.getValue().getSendingAttempts()).isEqualTo(1);
    }

    private EmailEntity createEmail(Consumer<EmailEntity> message) {
        EmailEntity email = EmailEntity.builder()
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
        if(message != null) {
            message.accept(email);
        }
        return email;
    }


    private IncomingEmailRequest createEmailRequest() {
        IncomingEmailRequest email = IncomingEmailRequest.builder()
                .withSenderEmail("test@hotmail.com")
                .withSenderName("test name")
                .withMessage("test message")
                .withSubject("subject")
                .withPartyId("party id")
                .withEmailAddress("test2@hotmail.com")
                .withHtmlMessage("test")
                .withAttachments(List.of(IncomingEmailRequest.Attachment.builder()
                        .withContent("content")
                        .withContentType("content type")
                        .withName("name")
                        .build()))
                .build();
        return email;
    }


}

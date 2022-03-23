package se.sundsvall.messaging.integration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.service.EmailService;

@ExtendWith(MockitoExtension.class)
class EmailPollerTest {

    @Mock
    private EmailService emailService;
    private EmailPoller emailPoller;

    @BeforeEach
    void setUp() {
        emailPoller = new EmailPoller(emailService);
    }

    @Test
    void whenRunning_thenCallsServiceSendOldestPending() {
        emailPoller.run();

        verify(emailService, times(1)).sendOldestPendingEmail();
    }
}

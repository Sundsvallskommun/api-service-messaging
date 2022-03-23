package se.sundsvall.messaging.integration;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.service.SmsService;

@ExtendWith(MockitoExtension.class)
class SmsPollerTest {

    @Mock
    private SmsService smsService;
    private SmsPoller smsPoller;

    @BeforeEach
    void setUp() {
        smsPoller = new SmsPoller(smsService);
    }

    @Test
    void whenRunning_thenCallsServiceSendOldestPending() {
        smsPoller.run();

        verify(smsService, times(1)).sendOldestPendingSms();
    }
}

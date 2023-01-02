package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class HistoryServiceTests {

    @Mock
    private DbIntegration mockDbIntegration;

    @InjectMocks
    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(mockDbIntegration);
    }

    @Test
    void test_getHistory() {
        when(mockDbIntegration.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of(History.builder().build()));

        var result = historyService.getHistory("someMessageId");

        assertThat(result).isNotEmpty();

        verify(mockDbIntegration, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getHistory_whenNoEntityExists() {
        when(mockDbIntegration.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of());

        var result = historyService.getHistory("someMessageId");

        assertThat(result).isEmpty();

        verify(mockDbIntegration, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getHistoryByBatchId() {
        when(mockDbIntegration.getHistoryByBatchId(any(String.class)))
            .thenReturn(List.of(History.builder().build()));

        var result = historyService.getHistoryByBatchId("someBatchId");

        assertThat(result).hasSize(1);

        verify(mockDbIntegration, times(1)).getHistoryByBatchId(any(String.class));
    }

    @Test
    void test_getConversationHistory() {
        when(mockDbIntegration.getHistory(any()))
            .thenReturn(List.of(History.builder().build()));

        var result = historyService.getConversationHistory("somePartyId", null, null);

        assertThat(result).hasSize(1);

        verify(mockDbIntegration, times(1)).getHistory(any());
    }
}

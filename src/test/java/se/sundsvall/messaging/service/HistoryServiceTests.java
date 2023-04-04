package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Test
    void test_getHistoryByMessageId() {
        when(mockDbIntegration.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of(History.builder().build()));

        var result = historyService.getHistoryByMessageId("someMessageId");

        assertThat(result).isNotEmpty();

        verify(mockDbIntegration, times(1)).getHistoryByMessageId(any(String.class));
    }

    @Test
    void test_getHistoryByMessageId_whenNoEntityExists() {
        when(mockDbIntegration.getHistoryByMessageId(any(String.class)))
            .thenReturn(List.of());

        var result = historyService.getHistoryByMessageId("someMessageId");

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
    void test_getHistoryByDeliveryId() {
        when(mockDbIntegration.getHistoryForDeliveryId(any(String.class)))
            .thenReturn(Optional.of(History.builder().build()));

        var result = historyService.getHistoryForDeliveryId("someBatchId");

        assertThat(result).isPresent();

        verify(mockDbIntegration, times(1)).getHistoryForDeliveryId(any(String.class));
    }

    @Test
    void test_getHistoryByDeliveryId_whenNoEntityExists() {
        when(mockDbIntegration.getHistoryForDeliveryId(any(String.class)))
            .thenReturn(Optional.empty());

        var result = historyService.getHistoryForDeliveryId("someBatchId");

        assertThat(result).isEmpty();

        verify(mockDbIntegration, times(1)).getHistoryForDeliveryId(any(String.class));
    }

    @Test
    void test_getConversationHistory() {
        when(mockDbIntegration.getHistory(any(String.class), nullable(LocalDate.class), nullable(LocalDate.class)))
            .thenReturn(List.of(History.builder().build()));

        var result = historyService.getConversationHistory("somePartyId", null, null);

        assertThat(result).hasSize(1);

        verify(mockDbIntegration, times(1))
            .getHistory(any(String.class), nullable(LocalDate.class), nullable(LocalDate.class));
    }
}

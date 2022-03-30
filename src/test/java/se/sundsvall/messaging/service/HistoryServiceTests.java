package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTests {

    @Mock
    private HistoryRepository mockHistoryRepository;

    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new HistoryService(mockHistoryRepository);
    }

    @Test
    void test_getHistory() {
        when(mockHistoryRepository.findById(any(String.class)))
            .thenReturn(Optional.of(HistoryEntity.builder().build()));

        var result = historyService.getHistory("someMessageId");

        assertThat(result).isPresent();

        verify(mockHistoryRepository, times(1)).findById(any(String.class));
    }

    @Test
    void test_getHistory_whenNoEntityExists() {
        when(mockHistoryRepository.findById(any(String.class)))
            .thenReturn(Optional.empty());

        var result = historyService.getHistory("someMessageId");

        assertThat(result).isEmpty();

        verify(mockHistoryRepository, times(1)).findById(any(String.class));
    }

    @Test
    void test_getHistoryByBatchId() {
        when(mockHistoryRepository.findByBatchIdEquals(any(String.class)))
            .thenReturn(List.of(HistoryEntity.builder().build()));

        var result = historyService.getHistoryByBatchId("someBatchId");

        assertThat(result).hasSize(1);

        verify(mockHistoryRepository, times(1)).findByBatchIdEquals(any(String.class));
    }

    @Test
    void test_getConversationHistory() {
        when(mockHistoryRepository.findAll(ArgumentMatchers.<Specification<HistoryEntity>>any()))
            .thenReturn(List.of(HistoryEntity.builder().build()));

        var result = historyService.getConversationHistory("somePartyId", null, null);

        assertThat(result).hasSize(1);

        verify(mockHistoryRepository, times(1)).findAll(ArgumentMatchers.<Specification<HistoryEntity>>any());
    }

    @Test
    void test_tooHistoryDto() {
        var historyEntity = HistoryEntity.builder()
            .withMessageId("someMessageId")
            .withBatchId("someBatchId")
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.PENDING)
            .withContent("someContent")
            .withCreatedAt(LocalDateTime.now())
            .build();

        var dto = historyService.toHistoryDto(historyEntity);

        assertThat(dto.getMessageId()).isEqualTo(historyEntity.getMessageId());
        assertThat(dto.getBatchId()).isEqualTo(historyEntity.getBatchId());
        assertThat(dto.getMessageType()).isEqualTo(historyEntity.getMessageType());
        assertThat(dto.getStatus()).isEqualTo(historyEntity.getStatus());
        assertThat(dto.getContent()).isEqualTo(historyEntity.getContent());
        assertThat(dto.getCreatedAt()).isEqualTo(historyEntity.getCreatedAt());
    }
}

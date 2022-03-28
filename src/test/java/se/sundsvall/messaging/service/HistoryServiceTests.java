package se.sundsvall.messaging.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTests {
    
    private static final String BATCH_ID = UUID.randomUUID().toString();
    private static final String MESSAGE_ID = UUID.randomUUID().toString();
    private static final String PARTY_ID = UUID.randomUUID().toString();

    @Mock
    private HistoryRepository mockHistoryRepository;

    private HistoryService service;

    @BeforeEach
    void setUp() {
        service = new HistoryService(mockHistoryRepository);
    }

    @Test
    void createHistory_givenSms_thenSaveAsHistory() {
        service.createHistory(SmsEntity.builder().build());

        verify(mockHistoryRepository, times(1)).save(any());
    }

    @Test
    void createHistory_givenEmail_thenSaveAsHistory() {
        service.createHistory(EmailEntity.builder().build());

        verify(mockHistoryRepository, times(1)).save(any());
    }

    @Test
    void createHistory_givenUndeliverable_thenSaveAsHistory() {
        service.createHistory(UndeliverableMessageDto.builder().build());

        verify(mockHistoryRepository, times(1)).save(any());
    }

    @Test
    void getHistoryByMessageId_whenHistoryExist_thenReturnsMatchingHistory() {
        var historyEntity = createHistory(history -> history.setMessageId(MESSAGE_ID));

        when(mockHistoryRepository.findByMessageIdEquals(anyString())).thenReturn(Optional.of(historyEntity));

        var historyDto = service.getHistoryByMessageId(MESSAGE_ID);

        assertThat(historyDto.getMessageId()).isEqualTo(MESSAGE_ID);
    }

    @Test
    void getHistoryByMessageId_whenNoHistoryExist_thenResourceNotFound() {
        assertThatThrownBy(() -> service.getHistoryByMessageId("unknown id"))
            .isInstanceOf(ThrowableProblem.class)
            .hasMessageContaining("Resource not found");
    }

    @Test
    void getHistoryByBatchId_whenHistoryExist_thenReturnsMatchingHistoryEntries() {
        var historyEntities = List.of(createHistory(history -> history.setBatchId(BATCH_ID)));

        when(mockHistoryRepository.findByBatchIdEquals(anyString())).thenReturn(historyEntities);

        var historyDtos = service.getHistoryByBatchId(BATCH_ID);

        assertThat(historyDtos).hasSize(1)
            .allMatch(history -> Objects.equals(history.getBatchId(), BATCH_ID));
    }

    @Test
    void getHistoryByBatchId_whenNoHistoryExist_thenReturnsEmptyList() {
        when(mockHistoryRepository.findByBatchIdEquals(anyString())).thenReturn(Collections.emptyList());

        assertThat(service.getHistoryByBatchId("")).isEmpty();
    }

    @Test
    void getHistoryForPartyId_whenHistoryExistsForPartyId_thenReturnDtoWithValuesFromEntity() {
        var historyEntities = List.of(createHistory(history -> history.setPartyId(PARTY_ID)));

        when(mockHistoryRepository.findByPartyIdEquals(anyString())).thenReturn(historyEntities);

        assertThat(service.getHistoryForPartyId(PARTY_ID)).hasSize(1);
    }

    private HistoryEntity createHistory(Consumer<HistoryEntity> modifier) {
        var entity = HistoryEntity.builder()
            .withBatchId(UUID.randomUUID().toString())
            .withMessageId(UUID.randomUUID().toString())
            .withPartyId(UUID.randomUUID().toString())
            .withExternalReferences(Map.of("key", "value"))
            .withSender("Sender name")
            .withMessage("Message content")
            .withPartyContact("john.doe@example.com")
            .withMessageType(MessageType.EMAIL)
            .withStatus(MessageStatus.SENT)
            .withCreatedAt(LocalDateTime.now())
            .build();

        if (modifier != null) {
            modifier.accept(entity);
        }

        return entity;
    }
}


package se.sundsvall.messaging.integration.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.test.annotation.UnitTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

@UnitTest
@ExtendWith(MockitoExtension.class)
class DbIntegrationTests {

    @Mock
    private MessageRepository mockMessageRepository;
    @Mock
    private HistoryRepository mockHistoryRepository;

    @Mock
    private StatisticsRepository mockStatisticsRepository;

    @InjectMocks
    private DbIntegration dbIntegration;

    @Test
    void test_getMessageByDeliveryId() {
        when(mockMessageRepository.findByDeliveryId(any(String.class)))
            .thenReturn(Optional.of(MessageEntity.builder().build()));

        assertThat(dbIntegration.getMessageByDeliveryId("someDeliveryId")).isPresent();

        verify(mockMessageRepository, times(1)).findByDeliveryId(any(String.class));
    }

    @Test
    void test_getLatestMessagesWithStatus() {
        when(mockMessageRepository.findLatestWithStatus(any(MessageStatus.class)))
            .thenReturn(List.of(MessageEntity.builder().build(), MessageEntity.builder().build()));

        assertThat(dbIntegration.getLatestMessagesWithStatus(PENDING)).hasSize(2);

        verify(mockMessageRepository, times(1)).findLatestWithStatus(any(MessageStatus.class));
    }

    @Test
    void test_saveMessage() {
        when(mockMessageRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder().build());

        assertThat(dbIntegration.saveMessage(Message.builder().build())).isNotNull();

        verify(mockMessageRepository, times(1)).save(any(MessageEntity.class));
    }

    @Test
    void test_saveMessages() {
        when(mockMessageRepository.save(any(MessageEntity.class))).thenReturn(MessageEntity.builder().build());

        assertThat(dbIntegration.saveMessages(List.of(Message.builder().build(), Message.builder().build()))).hasSize(2);

        verify(mockMessageRepository, times(2)).save(any(MessageEntity.class));
    }

    @Test
    void test_deleteMessageByDeliveryId() {
        doNothing().when(mockMessageRepository).deleteByDeliveryId(any(String.class));

        dbIntegration.deleteMessageByDeliveryId("someDeliveryId");

        verify(mockMessageRepository, times(1)).deleteByDeliveryId(any(String.class));
    }

    @Test
    void test_getHistoryByMessageId() {
        when(mockHistoryRepository.findByMessageId(any(String.class)))
            .thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

        assertThat(dbIntegration.getHistoryByMessageId("someMessageId")).hasSize(2);

        verify(mockHistoryRepository, times(1)).findByMessageId(any(String.class));
    }

    @Test
    void test_getHistory() {
        when(mockHistoryRepository.findAll(ArgumentMatchers.<Specification<HistoryEntity>>any()))
            .thenReturn(List.of(HistoryEntity.builder().build(), HistoryEntity.builder().build()));

        assertThat(dbIntegration.getHistory(null, null, null)).hasSize(2);

        verify(mockHistoryRepository, times(1))
            .findAll(ArgumentMatchers.<Specification<HistoryEntity>>any());
    }

    @Test
    void test_saveHistory() {
        when(mockHistoryRepository.save(any(HistoryEntity.class))).thenReturn(HistoryEntity.builder().build());

        assertThat(dbIntegration.saveHistory(Message.builder().build(), null)).isNotNull();

        verify(mockHistoryRepository, times(1)).save(any(HistoryEntity.class));
    }

    @Test
    void test_getStats() {
        final var from = LocalDate.now().minusDays(1);
        final var to = LocalDate.now();
        final var statsEntry = new StatsEntry(MESSAGE, MESSAGE, SENT);

        when(mockStatisticsRepository.getStats(MESSAGE, from, to)).thenReturn(List.of(statsEntry));

        assertThat(dbIntegration.getStats(MESSAGE, from, to)).isNotEmpty().hasSize(1).contains(statsEntry);

        verify(mockStatisticsRepository).getStats(MESSAGE, from, to);
    }

    @Test
    void test_getStatsByDepartment() {
        final var department = "department";
        final var from = LocalDate.now().minusDays(1);
        final var to = LocalDate.now();
        final var statsEntry = new StatsEntry(MESSAGE, MESSAGE, SENT, department);

        when(mockStatisticsRepository.getStatsByDepartment(department, MESSAGE, from, to)).thenReturn(List.of(statsEntry));

        assertThat(dbIntegration.getStatsByDepartment(department, MESSAGE, from, to)).isNotEmpty().hasSize(1).contains(statsEntry);

        verify(mockStatisticsRepository).getStatsByDepartment(department, MESSAGE, from, to);
    }

    @Test
    void test_mapToMessageWhenMessageEntityIsNull() {
        assertThat(dbIntegration.mapToMessage(null)).isNull();
    }

    @Test
    void test_mapToMessage() {
        var messageEntity = MessageEntity.builder()
            .withBatchId("someBatchId")
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withPartyId("somePartyId")
            .withType(SNAIL_MAIL)
            .withStatus(FAILED)
            .withContent("someContent")
            .build();

        var message = dbIntegration.mapToMessage(messageEntity);

        assertThat(message).isNotNull();
        assertThat(message.batchId()).isEqualTo(messageEntity.getBatchId());
        assertThat(message.messageId()).isEqualTo(messageEntity.getMessageId());
        assertThat(message.deliveryId()).isEqualTo(messageEntity.getDeliveryId());
        assertThat(message.partyId()).isEqualTo(messageEntity.getPartyId());
        assertThat(message.type()).isEqualTo(messageEntity.getType());
        assertThat(message.status()).isEqualTo(messageEntity.getStatus());
        assertThat(message.content()).isEqualTo(messageEntity.getContent());
    }

    @Test
    void test_mapToMessageEntityWhenMessageIsNull() {
        assertThat(dbIntegration.mapToMessageEntity(null)).isNull();
    }

    @Test
    void test_mapToMessageEntity() {
        var message = Message.builder()
            .withBatchId("someBatchId")
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withPartyId("somePartyId")
            .withType(SNAIL_MAIL)
            .withStatus(FAILED)
            .withContent("someContent")
            .build();

        var messageEntity = dbIntegration.mapToMessageEntity(message);

        assertThat(messageEntity).isNotNull();
        assertThat(messageEntity.getBatchId()).isEqualTo(message.batchId());
        assertThat(messageEntity.getMessageId()).isEqualTo(message.messageId());
        assertThat(messageEntity.getDeliveryId()).isEqualTo(message.deliveryId());
        assertThat(messageEntity.getPartyId()).isEqualTo(message.partyId());
        assertThat(messageEntity.getType()).isEqualTo(message.type());
        assertThat(messageEntity.getStatus()).isEqualTo(message.status());
        assertThat(messageEntity.getContent()).isEqualTo(message.content());
    }

    @Test
    void test_mapToHistoryWhenHistoryEntityIsNull() {
        assertThat(dbIntegration.mapToHistory((HistoryEntity) null)).isNull();
    }


    @Test
    void test_mapToHistoryWhenHistoryEntryIsNull() {
        assertThat(dbIntegration.mapToHistory((StatsEntry) null)).isNull();
    }

    @Test
    void test_mapToHistory() {
        var historyEntity = HistoryEntity.builder()
            .withBatchId("someBatchId")
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withPartyId("somePartyId")
            .withMessageType(SNAIL_MAIL)
            .withStatus(FAILED)
            .withContent("someContent")
            .withCreatedAt(LocalDateTime.now())
            .build();

        var history = dbIntegration.mapToHistory(historyEntity);

        assertThat(history).isNotNull();
        assertThat(history.batchId()).isEqualTo(historyEntity.getBatchId());
        assertThat(history.messageId()).isEqualTo(historyEntity.getMessageId());
        assertThat(history.deliveryId()).isEqualTo(historyEntity.getDeliveryId());
        assertThat(history.messageType()).isEqualTo(historyEntity.getMessageType());
        assertThat(history.status()).isEqualTo(historyEntity.getStatus());
        assertThat(history.content()).isEqualTo(historyEntity.getContent());
        assertThat(history.createdAt()).isEqualTo(historyEntity.getCreatedAt());
    }

    @Test
    void test_mapToHistoryEntityWhenMessageIsNull() {
        assertThat(dbIntegration.mapToHistoryEntity(null, null)).isNull();
    }

    @Test
    void test_mapToHistoryEntity() {
        var message = Message.builder()
            .withBatchId("someBatchId")
            .withMessageId("someMessageId")
            .withDeliveryId("someDeliveryId")
            .withPartyId("somePartyId")
            .withType(SNAIL_MAIL)
            .withStatus(FAILED)
            .withContent("someContent")
            .build();

        var historyEntity = dbIntegration.mapToHistoryEntity(message, "someStatusDetail");

        assertThat(historyEntity).isNotNull();
        assertThat(historyEntity.getBatchId()).isEqualTo(message.batchId());
        assertThat(historyEntity.getMessageId()).isEqualTo(message.messageId());
        assertThat(historyEntity.getDeliveryId()).isEqualTo(message.deliveryId());
        assertThat(historyEntity.getPartyId()).isEqualTo(message.partyId());
        assertThat(historyEntity.getMessageType()).isEqualTo(message.type());
        assertThat(historyEntity.getStatus()).isEqualTo(message.status());
        assertThat(historyEntity.getStatusDetail()).isEqualTo("someStatusDetail");
        assertThat(historyEntity.getContent()).isEqualTo(message.content());
        assertThat(historyEntity.getCreatedAt()).isNotNull();
    }
}

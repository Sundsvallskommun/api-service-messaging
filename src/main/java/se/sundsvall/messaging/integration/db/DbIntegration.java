package se.sundsvall.messaging.integration.db;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.orderByCreatedAtDesc;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withCreatedAtAfter;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withCreatedAtBefore;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withPartyId;

@Component
@Transactional
public class DbIntegration {

    private final MessageRepository messageRepository;
    private final HistoryRepository historyRepository;
    private final StatisticsRepository statisticsRepository;

    public DbIntegration(final MessageRepository messageRepository,
            final HistoryRepository historyRepository,
            final StatisticsRepository statisticsRepository) {
        this.messageRepository = messageRepository;
        this.historyRepository = historyRepository;
        this.statisticsRepository = statisticsRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Message> getMessageByDeliveryId(final String deliveryId) {
        return messageRepository.findByDeliveryId(deliveryId)
            .map(this::mapToMessage);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getLatestMessagesWithStatus(final MessageStatus status) {
        return messageRepository.findLatestWithStatus(status);
    }

    public Message saveMessage(final Message message) {
        return mapToMessage(messageRepository.save(mapToMessageEntity(message)));
    }

    public List<Message> saveMessages(final List<Message> messages) {
        return messages.stream()
            .map(this::mapToMessageEntity)
            .map(messageRepository::save)
            .map(this::mapToMessage)
            .toList();
    }

    public void deleteMessageByDeliveryId(final String deliveryId) {
        messageRepository.deleteByDeliveryId(deliveryId);
    }

    @Transactional(readOnly = true)
    public Optional<History> getHistoryForDeliveryId(final String deliveryId) {
        return historyRepository.findByDeliveryId(deliveryId)
            .map(this::mapToHistory);
    }

    @Transactional(readOnly = true)
    public List<History> getHistoryByMessageId(final String messageId) {
        return historyRepository.findByMessageId(messageId).stream()
            .map(this::mapToHistory)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<History> getHistoryByBatchId(final String batchId) {
        return historyRepository.findByBatchId(batchId).stream()
            .map(this::mapToHistory)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<History> getHistory(final String partyId, final LocalDate from, final LocalDate to) {
        var specifications = orderByCreatedAtDesc(
            withPartyId(partyId)
                .and(withCreatedAtAfter(from))
                .and(withCreatedAtBefore(to)));

        return historyRepository.findAll(specifications).stream()
            .map(this::mapToHistory)
            .toList();
    }

    public History saveHistory(final Message message, final String failureDetail) {
        return mapToHistory(historyRepository.save(mapToHistoryEntity(message, failureDetail)));
    }

    @Transactional(readOnly = true)
    public List<StatsEntry> getStats(final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return statisticsRepository.getStats(messageType, from, to);
    }

    @Transactional(readOnly = true)
    public List<StatsEntry> getStatsByDepartment(final String department, final MessageType messageType, final LocalDate from,
        final LocalDate to) {
        return statisticsRepository.getStatsByDepartment(department, messageType, from, to);
    }

    Message mapToMessage(final MessageEntity messageEntity) {
        if (null == messageEntity) {
            return null;
        }

        return Message.builder()
            .withBatchId(messageEntity.getBatchId())
            .withMessageId(messageEntity.getMessageId())
            .withDeliveryId(messageEntity.getDeliveryId())
            .withPartyId(messageEntity.getPartyId())
            .withType(messageEntity.getType())
            .withOriginalType(messageEntity.getOriginalMessageType())
            .withStatus(messageEntity.getStatus())
            .withContent(messageEntity.getContent())
            .build();
    }

    MessageEntity mapToMessageEntity(final Message message) {
        if (null == message) {
            return null;
        }

        return MessageEntity.builder()
            .withBatchId(message.batchId())
            .withMessageId(message.messageId())
            .withDeliveryId(message.deliveryId())
            .withPartyId(message.partyId())
            .withType(message.type())
            .withOriginalMessageType(message.originalType())
            .withStatus(message.status())
            .withContent(message.content())
            .build();
    }

    History mapToHistory(final HistoryEntity historyEntity) {
        if (null == historyEntity) {
            return null;
        }

        return History.builder()
            .withBatchId(historyEntity.getBatchId())
            .withMessageId(historyEntity.getMessageId())
            .withDeliveryId(historyEntity.getDeliveryId())
            .withMessageType(historyEntity.getMessageType())
            .withOriginalMessageType(historyEntity.getOriginalMessageType())
            .withStatus(historyEntity.getStatus())
            .withContent(historyEntity.getContent())
            .withCreatedAt(historyEntity.getCreatedAt())
            .build();
    }

    History mapToHistory(final StatsEntry historyEntry) {
        if (null == historyEntry) {
            return null;
        }

        return History.builder()
            .withMessageType(historyEntry.messageType())
            .withOriginalMessageType(historyEntry.originalMessageType())
            .withStatus(historyEntry.status())
            .build();
    }

    HistoryEntity mapToHistoryEntity(final Message message, final String statusDetail) {
        if (null == message) {
            return null;
        }

        return HistoryEntity.builder()
            .withBatchId(message.batchId())
            .withMessageId(message.messageId())
            .withDeliveryId(message.deliveryId())
            .withPartyId(message.partyId())
            .withMessageType(message.type())
            .withOriginalMessageType(message.originalType())
            .withStatus(message.status())
            .withStatusDetail(statusDetail)
            .withContent(message.content())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }
}

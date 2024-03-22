package se.sundsvall.messaging.integration.db;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.mapper.HistoryMapper;
import se.sundsvall.messaging.integration.db.mapper.MessageMapper;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.orderByCreatedAtDesc;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withCreatedAtAfter;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withCreatedAtBefore;
import static se.sundsvall.messaging.integration.db.HistoryRepository.Specs.withPartyId;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.mapToHistory;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.mapToHistoryEntity;
import static se.sundsvall.messaging.integration.db.mapper.MessageMapper.mapToMessage;
import static se.sundsvall.messaging.integration.db.mapper.MessageMapper.mapToMessageEntity;

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
            .map(MessageMapper::mapToMessage);
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
            .map(MessageMapper::mapToMessageEntity)
            .map(messageRepository::save)
            .map(MessageMapper::mapToMessage)
            .toList();
    }

    public void deleteMessageByDeliveryId(final String deliveryId) {
        messageRepository.deleteByDeliveryId(deliveryId);
    }

    @Transactional(readOnly = true)
    public Optional<History> getHistoryByDeliveryId(final String deliveryId) {
        return historyRepository.findByDeliveryId(deliveryId)
            .map(HistoryMapper::mapToHistory);
    }

    @Transactional(readOnly = true)
    public List<History> getHistoryByMessageId(final String messageId) {
        return historyRepository.findByMessageId(messageId).stream()
            .map(HistoryMapper::mapToHistory)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<History> getHistoryByBatchId(final String batchId) {
        return historyRepository.findByBatchId(batchId).stream()
            .map(HistoryMapper::mapToHistory)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<History> getHistory(final String partyId, final LocalDate from, final LocalDate to) {
        var specifications = orderByCreatedAtDesc(
            withPartyId(partyId)
                .and(withCreatedAtAfter(from))
                .and(withCreatedAtBefore(to)));

        return historyRepository.findAll(specifications).stream()
            .map(HistoryMapper::mapToHistory)
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
}

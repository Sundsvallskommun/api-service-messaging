package se.sundsvall.messaging.integration.db;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@Component
@Transactional
public class DbIntegration {

    private final MessageRepository messageRepository;
    private final HistoryRepository historyRepository;

    public DbIntegration(final MessageRepository messageRepository,
            final HistoryRepository historyRepository) {
        this.messageRepository = messageRepository;
        this.historyRepository = historyRepository;
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
    public List<StatsEntry> getStats(final MessageType messageType, final LocalDate from,
            final LocalDate to) {
        return historyRepository.getStats(messageType, from, to);
    }

    @Transactional(readOnly = true)
    public List<History> getHistory(final Specification<HistoryEntity> specification) {
        return historyRepository.findAll(specification).stream()
            .map(this::mapToHistory)
            .toList();
    }

    public History saveHistory(final Message message) {
        return mapToHistory(historyRepository.save(mapToHistoryEntity(message)));
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

    HistoryEntity mapToHistoryEntity(final Message message) {
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
            .withContent(message.content())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }
}

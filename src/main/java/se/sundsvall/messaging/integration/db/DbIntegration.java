package se.sundsvall.messaging.integration.db;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.integration.db.entity.CounterEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.Counter;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;

@Component
@Transactional
public class DbIntegration {

    private final MessageRepository messageRepository;
    private final HistoryRepository historyRepository;
    private final CounterRepository counterRepository;

    public DbIntegration(final MessageRepository messageRepository,
            final HistoryRepository historyRepository, final CounterRepository counterRepository) {
        this.messageRepository = messageRepository;
        this.historyRepository = historyRepository;
        this.counterRepository = counterRepository;
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
    public List<History> getHistory(final Specification<HistoryEntity> specification) {
        return historyRepository.findAll(specification).stream()
            .map(this::mapToHistory)
            .toList();
    }

    public History saveHistory(final Message message) {
        return mapToHistory(historyRepository.save(mapToHistoryEntity(message)));
    }

    @Transactional(readOnly = true)
    public List<Counter> getAllCounters() {
        return counterRepository.findAll().stream()
            .map(this::mapToCounter)
            .toList();
    }

    public Counter incrementAndSaveCounter(final String name) {
        var counter = counterRepository.findByName(name)
            .orElseGet(() -> CounterEntity.builder()
                .withName(name)
                .build())
            .increment();

        return mapToCounter(counterRepository.save(counter));
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
            .withStatus(historyEntity.getStatus())
            .withContent(historyEntity.getContent())
            .withCreatedAt(historyEntity.getCreatedAt())
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
            .withStatus(message.status())
            .withContent(message.content())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }

    Counter mapToCounter(final CounterEntity counterEntity) {
        if (null == counterEntity) {
            return null;
        }

        return Counter.builder()
            .withName(counterEntity.getName())
            .withValue(counterEntity.getValue())
            .build();
    }
}

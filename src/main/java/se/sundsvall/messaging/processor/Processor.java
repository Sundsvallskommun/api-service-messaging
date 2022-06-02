package se.sundsvall.messaging.processor;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;

public abstract class Processor {

    protected static final Gson GSON = new GsonBuilder().create();

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final MessageRepository messageRepository;
    protected final HistoryRepository historyRepository;

    protected Processor(final MessageRepository messageRepository,
            final HistoryRepository historyRepository) {
        this.messageRepository = messageRepository;
        this.historyRepository = historyRepository;
    }

    protected void handleSuccessfulDelivery(final MessageEntity message) {
        log.info("Successful delivery for {} {}", message.getType(), message.getMessageId());

        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.SENT)));
        messageRepository.deleteById(message.getMessageId());
    }

    protected void handleMaximumDeliveryAttemptsExceeded(final MessageEntity message) {
        log.info("Exceeded max sending attempts for {} {}", message.getType(), message.getMessageId());

        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.FAILED)));
        messageRepository.deleteById(message.getMessageId());
    }

    protected HistoryEntity mapToHistoryEntity(final MessageEntity message) {
        return HistoryEntity.builder()
            .withMessageId(message.getMessageId())
            .withBatchId(message.getBatchId())
            .withPartyId(message.getPartyId())
            .withMessageType(message.getType())
            .withStatus(message.getStatus())
            .withContent(message.getContent())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }
}

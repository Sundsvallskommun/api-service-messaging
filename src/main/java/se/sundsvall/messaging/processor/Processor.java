package se.sundsvall.messaging.processor;

import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.messaging.integration.db.CounterRepository;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.CounterEntity;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.WhitelistingService;

public abstract class Processor {

    protected static final Gson GSON = new GsonBuilder().create();

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final MessageRepository messageRepository;
    protected final HistoryRepository historyRepository;
    private final CounterRepository counterRepository;
    private final WhitelistingService whitelistingService;

    protected Processor(final MessageRepository messageRepository,
            final HistoryRepository historyRepository, final CounterRepository counterRepository) {
        this(messageRepository, historyRepository, counterRepository, null);
    }

    protected Processor(final MessageRepository messageRepository,
            final HistoryRepository historyRepository, final CounterRepository counterRepository,
            final WhitelistingService whitelistingService) {
        this.messageRepository = messageRepository;
        this.historyRepository = historyRepository;
        this.counterRepository = counterRepository;
        this.whitelistingService = whitelistingService;
    }

    @Transactional
    protected void handleSuccessfulDelivery(final MessageEntity message) {
        log.info("Successful delivery for {} (message id {}, delivery id {})",
            message.getType(), message.getMessageId(), message.getDeliveryId());

        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.SENT)));
        messageRepository.deleteByDeliveryId(message.getDeliveryId());

        incrementSuccessCounter(message.getType());
    }

    @Transactional
    protected void handleMaximumDeliveryAttemptsExceeded(final MessageEntity message) {
        log.info("Exceeded max sending attempts for {} (message id {}, delivery id {})",
            message.getType(), message.getMessageId(), message.getDeliveryId());

        historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.FAILED)));
        messageRepository.deleteByDeliveryId(message.getDeliveryId());

        incrementFailureCounter(message.getType());
    }

    protected boolean isWhitelisted(final MessageEntity message, final String recipient) {
        if (!whitelistingService.isWhitelisted(message.getType(), recipient)) {
            log.info("Recipient '{}' is not whitelisted for {} deliveries", recipient, message.getType());

            historyRepository.save(mapToHistoryEntity(message.withStatus(MessageStatus.NOT_WHITELISTED)));
            messageRepository.deleteByDeliveryId(message.getDeliveryId());

            incrementNotWhiteListedFailureCounter(message.getType());

            return false;
        }

        return true;
    }


    protected void incrementAttemptCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".total");
    }

    protected void incrementSuccessCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".success");
    }

    protected void incrementFailureCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".failure");
    }

    protected void incrementNotWhiteListedFailureCounter(final MessageType messageType) {
        incrementCounter(messageType.toString().toLowerCase() + ".failure.not-white-listed");
    }

    protected void incrementCounter(final String name) {
        var counter = counterRepository.findByName(name)
            .orElseGet(() -> CounterEntity.builder()
                .withName(name)
                .build())
            .increment();

        counterRepository.save(counter);
    }

    protected HistoryEntity mapToHistoryEntity(final MessageEntity message) {
        return HistoryEntity.builder()
            .withMessageId(message.getMessageId())
            .withBatchId(message.getBatchId())
            .withDeliveryId(message.getDeliveryId())
            .withPartyId(message.getPartyId())
            .withMessageType(message.getType())
            .withStatus(message.getStatus())
            .withContent(message.getContent())
            .withCreatedAt(LocalDateTime.now())
            .build();
    }
}

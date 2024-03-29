package se.sundsvall.messaging.service;

import org.springframework.stereotype.Service;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.History;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HistoryService {

    private final DbIntegration dbIntegration;

    public HistoryService(final DbIntegration dbIntegration) {
        this.dbIntegration = dbIntegration;
    }

    public List<History> getHistoryByMessageId(final String messageId) {
        return dbIntegration.getHistoryByMessageId(messageId);
    }

    public List<History> getHistoryByBatchId(final String batchId) {
        return dbIntegration.getHistoryByBatchId(batchId);
    }

    public Optional<History> getHistoryByDeliveryId(final String deliveryId) {
        return dbIntegration.getHistoryByDeliveryId(deliveryId);
    }

    public List<History> getConversationHistory(final String partyId, final LocalDate from,
            final LocalDate to) {
        return dbIntegration.getHistory(partyId, from, to);
    }
}

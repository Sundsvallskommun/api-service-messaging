package se.sundsvall.messaging.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.specification.HistoryEntitySpecifications;
import se.sundsvall.messaging.model.History;

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

    public Optional<History> getHistoryForDeliveryId(final String deliveryId) {
        return dbIntegration.getHistoryForDeliveryId(deliveryId);
    }

    public List<History> getConversationHistory(final String partyId, final LocalDate from,
            final LocalDate to) {
        var specifications = HistoryEntitySpecifications.withPartyId(partyId)
            .and(HistoryEntitySpecifications.withCreatedAtAfter(from))
            .and(HistoryEntitySpecifications.withCreatedAtBefore(to));

        return dbIntegration.getHistory(specifications);
    }
}

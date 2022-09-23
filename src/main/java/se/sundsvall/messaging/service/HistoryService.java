package se.sundsvall.messaging.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.specification.HistoryEntitySpecifications;

@Service
public class HistoryService {

    private final HistoryRepository repository;

    public HistoryService(final HistoryRepository repository) {
        this.repository = repository;
    }

    public List<HistoryDto> getHistory(final String messageId) {
        return repository.findByMessageId(messageId).stream()
            .map(this::toHistoryDto)
            .toList();
    }

    public List<HistoryDto> getHistoryByBatchId(final String batchId) {
        return repository.findByBatchId(batchId).stream()
            .map(this::toHistoryDto)
            .toList();
    }

    public List<HistoryDto> getConversationHistory(final String partyId, final LocalDate from,
            final LocalDate to) {
        var specifications = HistoryEntitySpecifications.withPartyId(partyId)
            .and(HistoryEntitySpecifications.withCreatedAtAfter(from))
            .and(HistoryEntitySpecifications.withCreatedAtBefore(to));

        return repository.findAll(specifications).stream()
            .map(this::toHistoryDto)
            .toList();
    }

    HistoryDto toHistoryDto(final HistoryEntity historyEntity) {
        return HistoryDto.builder()
            .withMessageId(historyEntity.getMessageId())
            .withBatchId(historyEntity.getBatchId())
            .withDeliveryId(historyEntity.getDeliveryId())
            .withMessageType(historyEntity.getMessageType())
            .withStatus(historyEntity.getStatus())
            .withContent(historyEntity.getContent())
            .withCreatedAt(historyEntity.getCreatedAt())
            .build();
    }
}

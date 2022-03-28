package se.sundsvall.messaging.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.messaging.dto.HistoryDto;
import se.sundsvall.messaging.dto.UndeliverableMessageDto;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.entity.EmailEntity;
import se.sundsvall.messaging.integration.db.entity.SmsEntity;
import se.sundsvall.messaging.mapper.HistoryMapper;

@Service
public class HistoryService {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryService.class);

    private final HistoryRepository repository;

    public HistoryService(HistoryRepository repository) {
        this.repository = repository;
    }

    public HistoryDto createHistory(final SmsEntity sms) {
        LOG.info("Creating history from SMS {}", sms.getMessageId());
        var history = repository.save(HistoryMapper.toHistory(sms));

        return HistoryMapper.toHistoryDto(history);
    }

    public HistoryDto createHistory(final EmailEntity email) {
        LOG.info("Creating history from E-mail {}", email.getMessageId());
        var history = repository.save(HistoryMapper.toHistory(email));

        return HistoryMapper.toHistoryDto(history);
    }

    public HistoryDto createHistory(final UndeliverableMessageDto undeliverable) {
        LOG.info("Creating history from undeliverable message {}", undeliverable.getMessageId());
        var history = repository.save(HistoryMapper.toHistory(undeliverable));

        return HistoryMapper.toHistoryDto(history);
    }

    public HistoryDto getHistoryByMessageId(final String id) {
        return repository.findByMessageIdEquals(id)
            .map(HistoryMapper::toHistoryDto)
            .orElseThrow(() -> Problem.builder()
                .withStatus(Status.NOT_FOUND)
                .withTitle("Resource not found")
                .withDetail("No history found for message id '" + id + "'")
                .build());
    }

    public List<HistoryDto> getHistoryByBatchId(final String batchId) {
        return repository.findByBatchIdEquals(batchId)
            .stream()
            .map(HistoryMapper::toHistoryDto)
            .collect(Collectors.toList());
    }

    public List<HistoryDto> getHistoryForPartyId(final String partyId) {
        return repository.findByPartyIdEquals(partyId)
            .stream()
            .map(HistoryMapper::toHistoryDto)
            .collect(Collectors.toList());
    }
}

package se.sundsvall.messaging.integration.db;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.integration.db.mapper.HistoryMapper;
import se.sundsvall.messaging.integration.db.mapper.MessageMapper;
import se.sundsvall.messaging.integration.db.projection.MessageIdProjection;
import se.sundsvall.messaging.integration.db.projection.StatsEntry;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.mapToHistory;
import static se.sundsvall.messaging.integration.db.mapper.HistoryMapper.mapToHistoryEntity;
import static se.sundsvall.messaging.integration.db.mapper.MessageMapper.mapToMessage;
import static se.sundsvall.messaging.integration.db.mapper.MessageMapper.mapToMessageEntity;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.orderByCreatedAtDesc;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withCreatedAtAfter;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withCreatedAtBefore;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withMunicipalityId;
import static se.sundsvall.messaging.integration.db.specification.HistorySpecification.withPartyId;

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
	public boolean existsByBatchId(final String batchId) {
		return messageRepository.existsByBatchId(batchId);
	}

	@Transactional(readOnly = true)
	public Optional<Message> getMessageByDeliveryId(final String deliveryId) {
		return messageRepository.findByDeliveryId(deliveryId)
			.map(MessageMapper::mapToMessage);
	}

	@Transactional(readOnly = true)
	public List<MessageEntity> getLatestMessagesWithStatus(final MessageStatus status) {
		return messageRepository.findByStatusOrderByCreatedAtAsc(status);
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
	public Optional<History> getHistoryByMunicipalityIdAndDeliveryId(String municipalityId, final String deliveryId) {
		return historyRepository.findByMunicipalityIdAndDeliveryId(municipalityId, deliveryId)
			.map(HistoryMapper::mapToHistory);
	}

	@Transactional(readOnly = true)
	public List<History> getHistoryByMunicipalityIdAndMessageId(String municipalityId, final String messageId) {
		return historyRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId).stream()
			.map(HistoryMapper::mapToHistory)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<History> getHistoryByMunicipalityIdAndBatchId(String municipalityId, final String batchId) {
		return historyRepository.findByMunicipalityIdAndBatchId(municipalityId, batchId).stream()
			.map(HistoryMapper::mapToHistory)
			.toList();
	}

	@Transactional(readOnly = true)
	public List<History> getHistory(final String municipalityId, final String partyId, final LocalDate from, final LocalDate to) {
		final var specifications = orderByCreatedAtDesc(
			withPartyId(partyId)
				.and(withMunicipalityId(municipalityId))
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
		final LocalDate to, final String municipalityId) {
		return statisticsRepository.getStats(messageType, from, to, municipalityId);
	}

	@Transactional(readOnly = true)
	public List<StatsEntry> getStatsByMunicipalityIdAndOriginAndDepartment(final String municipalityId, final String origin, final String department, final MessageType messageType, final LocalDate from,
		final LocalDate to) {
		return statisticsRepository.getStatsByMunicipalityIdAndyOriginAndDepartment(municipalityId, origin, department, messageType, from, to);
	}

	public Page<MessageIdProjection> getUniqueMessageIds(final String municipalityId, final String issuer, final LocalDateTime dateTime, final PageRequest pageRequest) {
		return historyRepository.findDistinctMessageIdsByMunicipalityIdAndIssuerAndCreatedAtIsAfter(municipalityId, issuer, dateTime, pageRequest);
	}

	public List<HistoryEntity> getHistoryEntityByMunicipalityIdAndMessageId(final String municipalityId, final String messageId) {
		return historyRepository.findByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	public HistoryEntity getFirstHistoryEntityByMunicipalityIdAndMessageId(final String municipalityId, final String messageId) {
		return historyRepository.findFirstByMunicipalityIdAndMessageId(municipalityId, messageId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No history found for message id " + messageId));
	}

}

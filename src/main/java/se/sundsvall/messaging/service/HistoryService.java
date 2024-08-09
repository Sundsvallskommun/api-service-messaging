package se.sundsvall.messaging.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.model.History;

@Service
public class HistoryService {

	private final DbIntegration dbIntegration;

	public HistoryService(final DbIntegration dbIntegration) {
		this.dbIntegration = dbIntegration;
	}

	public List<History> getHistoryByMunicipalityIdAndMessageId(final String municipalityId, final String messageId) {
		return dbIntegration.getHistoryByMunicipalityIdAndMessageId(municipalityId, messageId);
	}

	public List<History> getHistoryByMunicipalityIdAndBatchId(final String municipalityId, final String batchId) {
		return dbIntegration.getHistoryByMunicipalityIdAndBatchId(municipalityId, batchId);
	}

	public Optional<History> getHistoryByMunicipalityIdAndDeliveryId(final String municipalityId, final String deliveryId) {
		return dbIntegration.getHistoryByMunicipalityIdAndDeliveryId(municipalityId, deliveryId);
	}

	public List<History> getConversationHistory(final String municipalityId, final String partyId, final LocalDate from,
		final LocalDate to) {
		return dbIntegration.getHistory(municipalityId, partyId, from, to);
	}

}

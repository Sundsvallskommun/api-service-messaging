package se.sundsvall.messaging.integration.db.mapper;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;

import com.google.gson.JsonParser;

import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.Message;

public final class HistoryMapper {

	private static final String DEPARTMENT = "department";

	private HistoryMapper() {
		// Intentionally empty to prevent instantiation
	}

	public static History mapToHistory(final HistoryEntity historyEntity) {
		return ofNullable(historyEntity).map(entity -> History.builder()
			.withBatchId(historyEntity.getBatchId())
			.withMessageId(historyEntity.getMessageId())
			.withDeliveryId(historyEntity.getDeliveryId())
			.withMessageType(historyEntity.getMessageType())
			.withOriginalMessageType(historyEntity.getOriginalMessageType())
			.withStatus(historyEntity.getStatus())
			.withContent(historyEntity.getContent())
			.withCreatedAt(historyEntity.getCreatedAt())
			.withOrigin(historyEntity.getOrigin())
			.withIssuer(historyEntity.getIssuer())
			.withMunicipalityId(historyEntity.getMunicipalityId())
			.build()).orElse(null);
	}

	public static HistoryEntity mapToHistoryEntity(final Message message, final String statusDetail) {
		return ofNullable(message).map(message1 -> HistoryEntity.builder()
			.withBatchId(message.batchId())
			.withMessageId(message.messageId())
			.withDeliveryId(message.deliveryId())
			.withPartyId(message.partyId())
			.withMessageType(message.type())
			.withOriginalMessageType(message.originalType())
			.withStatus(message.status())
			.withStatusDetail(statusDetail)
			.withContent(message.content())
			.withOrigin(message.origin())
			.withIssuer(message.issuer())
			.withDepartment(toDepartment(message.content()))
			.withCreatedAt(LocalDateTime.now())
			.withMunicipalityId(message.municipalityId())
			.build()).orElse(null);
	}

	private static String toDepartment(final String content) {
		if (null == content) {
			return null;
		}

		final var jsonObject = JsonParser.parseString(content).getAsJsonObject();

		return jsonObject.has(DEPARTMENT) ? jsonObject.get(DEPARTMENT).getAsString() : null;
	}

}

package se.sundsvall.messaging.integration.db.mapper;

import static java.util.Optional.ofNullable;

import com.google.gson.JsonParser;
import java.time.LocalDateTime;
import java.util.Collections;
import org.springframework.data.domain.Page;
import se.sundsvall.dept44.models.api.paging.PagingAndSortingMetaData;
import se.sundsvall.messaging.api.model.response.Batch;
import se.sundsvall.messaging.api.model.response.UserBatches;
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
		return ofNullable(message).map(actualMessage -> HistoryEntity.builder()
			.withBatchId(actualMessage.batchId())
			.withMessageId(actualMessage.messageId())
			.withDeliveryId(actualMessage.deliveryId())
			.withPartyId(actualMessage.partyId())
			.withMessageType(actualMessage.type())
			.withOriginalMessageType(actualMessage.originalType())
			.withStatus(actualMessage.status())
			.withStatusDetail(statusDetail)
			.withContent(actualMessage.content())
			.withOrigin(actualMessage.origin())
			.withIssuer(actualMessage.issuer())
			.withDepartment(toDepartment(actualMessage.content()))
			.withCreatedAt(LocalDateTime.now())
			.withMunicipalityId(actualMessage.municipalityId())
			.withDestinationAddress(actualMessage.address())
			.build()).orElse(null);
	}

	private static String toDepartment(final String content) {
		if (null == content) {
			return null;
		}

		final var jsonObject = JsonParser.parseString(content).getAsJsonObject();

		return jsonObject.has(DEPARTMENT) ? jsonObject.get(DEPARTMENT).getAsString() : null;
	}

	public static Batch toBatch(final String batchId, final LocalDateTime sent, final String messageType, final String subject, final int attachmentCount, final int recipientCount, final Batch.Status status) {
		return Batch.builder()
			.withAttachmentCount(attachmentCount)
			.withBatchId(batchId)
			.withMessageType(messageType)
			.withRecipientCount(recipientCount)
			.withSent(sent)
			.withStatus(status)
			.withSubject(subject)
			.build();
	}

	public static Batch.Status toStatus(final int successful, final int unsuccessful) {
		return Batch.Status.builder()
			.withSuccessful(successful)
			.withUnsuccessful(unsuccessful)
			.build();
	}

	public static UserBatches toUserBatches(final Page<Batch> pagedBatch, final int page) {
		return UserBatches.builder()
			.withBatches(pagedBatch.getTotalPages() < page ? Collections.emptyList() : pagedBatch.getContent())
			.withMetaData(PagingAndSortingMetaData.create().withPageData(pagedBatch))
			.build();
	}
}

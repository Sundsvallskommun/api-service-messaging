package se.sundsvall.messaging.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.projection.BatchHistoryProjection;
import se.sundsvall.messaging.model.MessageType;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.util.JsonUtils.fromJson;

@Component
public class BatchExtractor {

	private static final String EMPTY_STRING = "";
	private static final List<MessageType> TYPES_WITH_ATTACHMENTS = List.of(DIGITAL_MAIL, SNAIL_MAIL, WEB_MESSAGE, EMAIL);
	private static final List<MessageType> TYPES_WITH_SUBJECT = List.of(DIGITAL_MAIL, EMAIL);

	private final DbIntegration dbIntegration;

	public BatchExtractor(DbIntegration dbIntegration) {
		this.dbIntegration = dbIntegration;
	}

	/**
	 * Helper method for calculating total amount of recipients for messages sent in the batch
	 *
	 * @param  projections Messages in the batch to calculate total amount of recipients for.
	 * @return             Total amount of message recipients in the batch.
	 */
	public int extractRecipientCount(List<BatchHistoryProjection> projections) {
		return ofNullable(projections).orElse(emptyList()).stream()
			.collect(groupingBy(BatchHistoryProjection::getMessageId))
			.size();
	}

	/**
	 * Helper method for calculating amount of successful transmissions of messages in the batch
	 *
	 * @param  projections Messages in the batch calculate successful transmissions for.
	 * @return             Amount of successful transmissions in the batch.
	 */
	public int extractSuccessfulCount(List<BatchHistoryProjection> projections) {
		return ofNullable(projections).orElse(emptyList()).stream()
			.filter(projection -> Objects.equals(SENT, projection.getStatus()))
			.collect(groupingBy(BatchHistoryProjection::getMessageId))
			.size();
	}

	/**
	 * Helper method for calculating amount of unsuccessful transmissions of messages in the batch
	 *
	 * @param  projections Messages in the batch calculate unsuccessful transmissions for.
	 * @return             Amount of unsuccessful transmissions in the batch.
	 */
	public int extractUnsuccessfulCount(List<BatchHistoryProjection> projections) {
		return ofNullable(projections).orElse(emptyList()).stream()
			.filter(projection -> ObjectUtils.notEqual(SENT, projection.getStatus()))
			.filter(projection -> ofNullable(projections).orElse(emptyList()).stream()
				.filter(successProjection -> Objects.equals(SENT, successProjection.getStatus()))
				.noneMatch(successBhp -> Objects.equals(projection.getMessageId(), successBhp.getMessageId())))
			.collect(groupingBy(BatchHistoryProjection::getMessageId))
			.size();
	}

	/**
	 * Helper method for extracting subject for messages in batch by fetching first message of type
	 * DIGITAL_MAIL or EMAIL and reading full instance of the entity from history table, which is
	 * then deserialized to return the subject attribute (as only digital mail and email contains this
	 * attribute).
	 *
	 * @param  projections Messages in the batch to extract attachment size for.
	 * @return             The size of attached documents for the first member in list that matches criteria (all
	 *                     members matching criteria has the same attachment size).
	 */
	public String extractSubject(String municipalityId, List<BatchHistoryProjection> projections) {
		final var stringBuilder = new StringBuilder();

		ofNullable(projections).orElse(emptyList()).stream()
			.filter(projection -> containsAny(TYPES_WITH_SUBJECT, projection.getMessageType()))
			.map(BatchHistoryProjection::getMessageId)
			.map(messageId -> dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(municipalityId, messageId, TYPES_WITH_SUBJECT))
			.findAny()
			.ifPresent(historyEntity -> stringBuilder.append(switch (historyEntity.getMessageType())
			{
				case DIGITAL_MAIL -> ofNullable(fromJson(historyEntity.getContent(), DigitalMailRequest.class)).map(DigitalMailRequest::subject).orElse(null);
				case EMAIL -> ofNullable(fromJson(historyEntity.getContent(), EmailRequest.class)).map(EmailRequest::subject).orElse(null);
				default -> EMPTY_STRING;
			}));

		return stringBuilder.toString();
	}

	/**
	 * Helper method for calculating attached documents for messages in batch by fetching first message of type
	 * DIGITAL_MAIL, SNAIL_MAIL, WEB_MESSAGE or EMAIL and reading full instance of the entity from history table, which is
	 * then deserialized to return attachment list size.
	 *
	 * @param  projections Messages in the batch to extract attachment size for.
	 * @return             The size of attached documents for the first member in list that matches criteria (all
	 *                     members matching criteria has the same attachment size).
	 */
	public int extractAttachmentCount(String municipalityId, List<BatchHistoryProjection> projections) {
		final var amount = new AtomicInteger();

		ofNullable(projections).orElse(emptyList()).stream()
			.filter(projection -> containsAny(TYPES_WITH_ATTACHMENTS, projection.getMessageType()))
			.map(BatchHistoryProjection::getMessageId)
			.map(messageId -> dbIntegration.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(municipalityId, messageId, TYPES_WITH_ATTACHMENTS))
			.findAny()
			.ifPresent(historyEntity -> amount.set(switch (historyEntity.getMessageType())
			{
				case DIGITAL_MAIL -> ofNullable(fromJson(historyEntity.getContent(), DigitalMailRequest.class)).map(DigitalMailRequest::attachments).map(List::size).orElse(0);
				case SNAIL_MAIL -> ofNullable(fromJson(historyEntity.getContent(), SnailMailRequest.class)).map(SnailMailRequest::attachments).map(List::size).orElse(0);
				case WEB_MESSAGE -> ofNullable(fromJson(historyEntity.getContent(), WebMessageRequest.class)).map(WebMessageRequest::attachments).map(List::size).orElse(0);
				case EMAIL -> ofNullable(fromJson(historyEntity.getContent(), EmailRequest.class)).map(EmailRequest::attachments).map(List::size).orElse(0);
				default -> 0;
			}));

		return amount.intValue();
	}

	/**
	 * Helper method for extracting original message type from the messages attached to the batch.
	 *
	 * @param  projections The batch to extract original message type for.
	 * @return             Any non null original message type found in list (all members has the same original
	 *                     message type) or null if no member has value for the attribute.
	 */
	public String extractOriginalMesageType(List<BatchHistoryProjection> projections) {
		return ofNullable(projections).orElse(emptyList()).stream()
			.map(BatchHistoryProjection::getOriginalMessageType)
			.filter(Objects::nonNull)
			.map(MessageType::name)
			.findAny()
			.orElse(null);
	}

	/**
	 * Helper method for extracting an approximate timestamp when messages was sent, prioritizing successful
	 * transmission and falling back to any non null created at value if no status for successful send with
	 * present created at value is found.
	 *
	 * @param  projections Messages in the batch to extract timestamp from.
	 * @return             Timestamp corresponding to successful send in first place, or any non null timestamp if no
	 *                     successful transmission can be found in the batch (or null if no date at all is present).
	 */
	public LocalDateTime extractSent(List<BatchHistoryProjection> projections) {
		return ofNullable(projections).orElse(emptyList()).stream()
			.filter(projection -> Objects.equals(SENT, projection.getStatus()))
			.map(BatchHistoryProjection::getCreatedAt)
			.filter(Objects::nonNull)
			.findAny()
			.or(() -> ofNullable(projections).orElse(emptyList()).stream()
				.map(BatchHistoryProjection::getCreatedAt)
				.filter(Objects::nonNull)
				.findAny())
			.orElse(null);
	}
}

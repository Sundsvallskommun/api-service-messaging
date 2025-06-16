package se.sundsvall.messaging.service;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.messaging.integration.db.DbIntegration;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.integration.db.projection.BatchHistoryProjection;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

@ExtendWith(MockitoExtension.class)
class BatchExtractorTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final String MESSAGE_ID = "messageId";
	private static final String SUBJECT = "subject";
	private static final String EMPTY_STRING = "";

	@Mock
	private DbIntegration dbIntegrationMock;

	@InjectMocks
	private BatchExtractor extractor;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(dbIntegrationMock);
	}

	@ParameterizedTest
	@MethodSource("extractRecipientCountArgumentProvider")
	void extractRecipientCount(List<BatchHistoryProjection> projections, int expected) {
		assertThat(extractor.extractRecipientCount(projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractRecipientCountArgumentProvider() {
		return Stream.of(
			Arguments.of(null, 0),
			Arguments.of(emptyList(), 0),
			Arguments.of(List.of(createBatchHistoryProjection("1")), 1),
			Arguments.of(List.of(createBatchHistoryProjection("1"), createBatchHistoryProjection("1")), 1),
			Arguments.of(List.of(createBatchHistoryProjection("1"), createBatchHistoryProjection("2")), 2));
	}

	@ParameterizedTest
	@MethodSource("extractSuccessfulCountArgumentProvider")
	void extractSuccessfulCount(List<BatchHistoryProjection> projections, int expected) {
		assertThat(extractor.extractSuccessfulCount(projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractSuccessfulCountArgumentProvider() {
		final Map<List<BatchHistoryProjection>, Integer> scenarios = new HashMap<>(
			List.of(MessageStatus.values()).stream()
				.filter(status -> ObjectUtils.notEqual(SENT, status))
				.map(status -> createBatchHistoryProjection("1", status))
				.map(List::of)
				.map(list -> Map.entry(list, 0))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		scenarios.put(null, 0);
		scenarios.put(emptyList(), 0);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT)), 1);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT), createBatchHistoryProjection("1", FAILED)), 1);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT), createBatchHistoryProjection("2", FAILED)), 1);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT), createBatchHistoryProjection("2", SENT)), 2);

		return scenarios.entrySet().stream()
			.map(entry -> Arguments.of(entry.getKey(), entry.getValue()))
			.toList().stream();
	}

	@ParameterizedTest
	@MethodSource("extractUnsuccessfulCountArgumentProvider")
	void extractUnsuccessfulCount(List<BatchHistoryProjection> projections, int expected) {
		assertThat(extractor.extractUnsuccessfulCount(projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractUnsuccessfulCountArgumentProvider() {
		final Map<List<BatchHistoryProjection>, Integer> scenarios = new HashMap<>(
			List.of(MessageStatus.values()).stream()
				.filter(status -> ObjectUtils.notEqual(SENT, status))
				.map(status -> createBatchHistoryProjection("1", status))
				.map(List::of)
				.map(list -> Map.entry(list, 1))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue)));

		scenarios.put(null, 0);
		scenarios.put(emptyList(), 0);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT)), 0);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT), createBatchHistoryProjection("1", FAILED)), 0);
		scenarios.put(List.of(createBatchHistoryProjection("1", SENT), createBatchHistoryProjection("2", FAILED)), 1);
		scenarios.put(List.of(createBatchHistoryProjection("1", FAILED), createBatchHistoryProjection("2", FAILED)), 2);

		return scenarios.entrySet().stream()
			.map(entry -> Arguments.of(entry.getKey(), entry.getValue()))
			.toList().stream();
	}

	@ParameterizedTest
	@MethodSource("extractSubjectWhenCorrectTypeArgumentProvider")
	void extractSubjectWhenCorrectType(List<BatchHistoryProjection> projections, MessageType messageType, String expected) {
		when(dbIntegrationMock.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(eq(MUNICIPALITY_ID), eq(MESSAGE_ID), anyList())).thenReturn(HistoryEntity.builder()
			.withMessageType(messageType)
			.withContent("{\"subject\": \"%s\"}".formatted(SUBJECT))
			.build());

		assertThat(extractor.extractSubject(MUNICIPALITY_ID, projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractSubjectWhenCorrectTypeArgumentProvider() {
		return Stream.of(
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, null, EMAIL)), EMAIL, SUBJECT),
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, null, DIGITAL_MAIL)), DIGITAL_MAIL, SUBJECT),
			Arguments.of(List.of(createBatchHistoryProjection("otherMessageId", null, SNAIL_MAIL), createBatchHistoryProjection(MESSAGE_ID, null, DIGITAL_MAIL)), DIGITAL_MAIL, SUBJECT));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@MethodSource("extractSubjectWhenIncorrectTypeArgumentProvider")
	void extractSubjectWhenIncorrectType(List<BatchHistoryProjection> projections) {
		assertThat(extractor.extractSubject(MUNICIPALITY_ID, projections)).isEqualTo(EMPTY_STRING);
	}

	private static Stream<Arguments> extractSubjectWhenIncorrectTypeArgumentProvider() {
		return List.of(MessageType.values()).stream()
			.filter(type -> ObjectUtils.notEqual(EMAIL, type))
			.filter(type -> ObjectUtils.notEqual(DIGITAL_MAIL, type))
			.map(type -> createBatchHistoryProjection(MESSAGE_ID, null, type))
			.map(List::of)
			.map(Arguments::of);
	}

	@Test
	void extractSubjectWhenEntityDiffersInType() {
		when(dbIntegrationMock.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(eq(MUNICIPALITY_ID), eq(MESSAGE_ID), anyList())).thenReturn(HistoryEntity.builder()
			.withMessageType(SNAIL_MAIL)
			.build());

		assertThat(extractor.extractSubject(MUNICIPALITY_ID, List.of(createBatchHistoryProjection(MESSAGE_ID, null, DIGITAL_MAIL)))).isEqualTo(EMPTY_STRING);
	}

	@ParameterizedTest
	@MethodSource("extractAttchmentCountWhenCorrectTypeArgumentProvider")
	void extractAttchmentCountWhenCorrectType(List<BatchHistoryProjection> projections, MessageType messageType, int attachmentSize) {
		final var attachmentContent = ",{\"contentType\": \"application/pdf\"}".repeat(attachmentSize).substring(1);

		when(dbIntegrationMock.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(eq(MUNICIPALITY_ID), eq(MESSAGE_ID), anyList())).thenReturn(HistoryEntity.builder()
			.withMessageType(messageType)
			.withContent("{\"attachments\": [%s] }".formatted(attachmentContent))
			.build());

		assertThat(extractor.extractAttchmentCount(MUNICIPALITY_ID, projections)).isEqualTo(attachmentSize);
	}

	private static Stream<Arguments> extractAttchmentCountWhenCorrectTypeArgumentProvider() {
		return Stream.of(
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, EMAIL)), EMAIL, 1),
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, WEB_MESSAGE)), WEB_MESSAGE, 2),
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, SNAIL_MAIL)), SNAIL_MAIL, 3),
			Arguments.of(List.of(createBatchHistoryProjection(MESSAGE_ID, DIGITAL_MAIL)), DIGITAL_MAIL, 4));
	}

	@ParameterizedTest
	@NullAndEmptySource
	@MethodSource("extractAttchmentCountWhenIncorrectTypeArgumentProvider")
	void extractAttchmentCountWhenIncorrectType(List<BatchHistoryProjection> projections) {
		assertThat(extractor.extractAttchmentCount(MUNICIPALITY_ID, projections)).isZero();
	}

	private static Stream<Arguments> extractAttchmentCountWhenIncorrectTypeArgumentProvider() {
		return List.of(MessageType.values()).stream()
			.filter(type -> ObjectUtils.notEqual(EMAIL, type))
			.filter(type -> ObjectUtils.notEqual(WEB_MESSAGE, type))
			.filter(type -> ObjectUtils.notEqual(SNAIL_MAIL, type))
			.filter(type -> ObjectUtils.notEqual(DIGITAL_MAIL, type))
			.map(type -> createBatchHistoryProjection(MESSAGE_ID, null, type))
			.map(List::of)
			.map(Arguments::of);
	}

	@Test
	void extractAttchmentCountWhenEntityDiffersInType() {
		when(dbIntegrationMock.getFirstHistoryEntityByMunicipalityIdAndMessageIdAndTypeIn(eq(MUNICIPALITY_ID), eq(MESSAGE_ID), anyList())).thenReturn(HistoryEntity.builder()
			.withMessageType(SMS)
			.build());

		assertThat(extractor.extractAttchmentCount(MUNICIPALITY_ID, List.of(createBatchHistoryProjection(MESSAGE_ID, null, DIGITAL_MAIL)))).isZero();
	}

	@ParameterizedTest
	@MethodSource("extractOrignalMesageTypeArgumentProvider")
	void extractOrignalMesageType(List<BatchHistoryProjection> projections, String expected) {
		assertThat(extractor.extractOrignalMesageType(projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractOrignalMesageTypeArgumentProvider() {
		return Stream.of(
			Arguments.of(null, null),
			Arguments.of(emptyList(), null),
			Arguments.of(List.of(createBatchHistoryProjection(null)), null),
			Arguments.of(List.of(createBatchHistoryProjection(null, null, null, SNAIL_MAIL)), SNAIL_MAIL.name()),
			Arguments.of(List.of(createBatchHistoryProjection(null, null, SNAIL_MAIL, DIGITAL_MAIL)), DIGITAL_MAIL.name()));
	}

	@ParameterizedTest
	@MethodSource("extractSentArgumentProvider")
	void extractSent(List<BatchHistoryProjection> projections, LocalDateTime expected) {
		assertThat(extractor.extractSent(projections)).isEqualTo(expected);
	}

	private static Stream<Arguments> extractSentArgumentProvider() {
		final var timeStamp = LocalDateTime.now();

		return Stream.of(
			Arguments.of(null, null),
			Arguments.of(emptyList(), null),
			Arguments.of(List.of(createBatchHistoryProjection(null)), null),
			Arguments.of(List.of(createBatchHistoryProjection(SENT, timeStamp)), timeStamp),
			Arguments.of(List.of(createBatchHistoryProjection(null, timeStamp)), timeStamp),
			Arguments.of(List.of(createBatchHistoryProjection(SENT, null), createBatchHistoryProjection(null, timeStamp)), timeStamp));
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId) {
		return createBatchHistoryProjection(messageId, (MessageStatus) null);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, MessageType type) {
		return createBatchHistoryProjection(messageId, null, type);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, MessageStatus status) {
		return createBatchHistoryProjection(messageId, status, null);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, MessageStatus status, MessageType messageType, MessageType originalMessageType) {
		return createBatchHistoryProjection(messageId, status, messageType, originalMessageType, null);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, MessageStatus status, MessageType messageType) {
		return createBatchHistoryProjection(messageId, status, messageType, null, null);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(MessageStatus messageStatus, LocalDateTime createdAt) {
		return createBatchHistoryProjection(null, messageStatus, null, null, createdAt);
	}

	private static BatchHistoryProjection createBatchHistoryProjection(String messageId, MessageStatus status, MessageType messageType, MessageType originalMessageType, LocalDateTime createdAt) {
		return new BatchHistoryProjection() {

			@Override
			public MessageStatus getStatus() {
				return status;
			}

			@Override
			public MessageType getOriginalMessageType() {
				return originalMessageType;
			}

			@Override
			public MessageType getMessageType() {
				return messageType;
			}

			@Override
			public String getMessageId() {
				return messageId;
			}

			@Override
			public LocalDateTime getCreatedAt() {
				return createdAt;
			}

			@Override
			public String getBatchId() {
				return null;
			}
		};
	}
}

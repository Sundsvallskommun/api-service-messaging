package se.sundsvall.messaging.api;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_METADATA_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_STATUS_PATH;
import static se.sundsvall.messaging.Constants.USER_BATCHES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGE_PATH;
import static se.sundsvall.messaging.TestDataFactory.createUserMessages;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.SMS;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.Batch;
import se.sundsvall.messaging.api.model.response.DeliveryResult;
import se.sundsvall.messaging.api.model.response.HistoryResponse;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.api.model.response.UserBatches;
import se.sundsvall.messaging.api.model.response.UserMessage;
import se.sundsvall.messaging.api.model.response.UserMessages;
import se.sundsvall.messaging.model.History;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.HistoryService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class HistoryResourceTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockitoBean
	private HistoryService mockHistoryService;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void getConversationHistoryWithoutDates(final MessageType messageType) {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withMessageType(messageType)
			.build();
		when(mockHistoryService.getConversationHistory(any(), any(), any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH).build(Map.of("partyId", partyId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(HistoryResponse::messageType).containsExactly(messageType);

		verify(mockHistoryService).getConversationHistory(MUNICIPALITY_ID, partyId, null, null);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@ParameterizedTest
	@EnumSource(MessageType.class)
	void getConversationHistoryWithDates(final MessageType messageType) {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		final var fromDate = LocalDate.now().minusDays(30);
		final var toDate = LocalDate.now().minusDays(15);
		final var history = History.builder()
			.withMessageType(messageType)
			.build();
		when(mockHistoryService.getConversationHistory(any(), any(), any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH)
				.queryParam("from", fromDate)
				.queryParam("to", toDate)
				.build(Map.of("partyId", partyId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(HistoryResponse::messageType).containsExactly(messageType);

		verify(mockHistoryService).getConversationHistory(MUNICIPALITY_ID, partyId, fromDate, toDate);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getConversationHistoryWhenNoHistoryExists() {
		// Arrange
		final var partyId = UUID.randomUUID().toString();
		when(mockHistoryService.getConversationHistory(any(), any(), any(), any())).thenReturn(emptyList());

		// Act
		final var result = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH).build(Map.of("partyId", partyId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(History.class)
			.returnResult()
			.getResponseBody();

		assertThat(result).isEmpty();

		// Assert and verify
		verify(mockHistoryService).getConversationHistory(MUNICIPALITY_ID, partyId, null, null);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getDeliveryStatus() {
		// Arrange
		final var deliveryId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withDeliveryId("deliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMunicipalityIdAndDeliveryId(any(), any())).thenReturn(Optional.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(DELIVERY_STATUS_PATH).build(Map.of("deliveryId", deliveryId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(DeliveryResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasAllNullFieldsOrPropertiesExcept("deliveryId", "messageType", "status");
		assertThat(response.deliveryId()).isEqualTo(history.deliveryId());
		assertThat(response.messageType()).isEqualTo(history.messageType());
		assertThat(response.status()).isEqualTo(history.status());

		verify(mockHistoryService).getHistoryByMunicipalityIdAndDeliveryId(MUNICIPALITY_ID, deliveryId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getDeliveryStatusWhenNoStatusExists() {
		// Arrange
		final var deliveryId = UUID.randomUUID().toString();

		when(mockHistoryService.getHistoryByMunicipalityIdAndDeliveryId(any(), any())).thenReturn(Optional.empty());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(DELIVERY_STATUS_PATH).build(Map.of("deliveryId", deliveryId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMunicipalityIdAndDeliveryId(MUNICIPALITY_ID, deliveryId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getBatchStatus() {
		// Arrange
		final var batchId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId(batchId)
			.withMessageId("someMessageId")
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMunicipalityIdAndBatchId(any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BATCH_STATUS_PATH).build(Map.of("batchId", batchId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MessageBatchResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(MessageBatchResult::batchId).containsExactly(batchId);

		verify(mockHistoryService).getHistoryByMunicipalityIdAndBatchId(MUNICIPALITY_ID, batchId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getBatchStatusWhenNoHistoryExists() {
		// Arrange
		final var batchId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByMunicipalityIdAndBatchId(any(), any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BATCH_STATUS_PATH).build(Map.of("batchId", batchId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMunicipalityIdAndBatchId(MUNICIPALITY_ID, batchId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getMessageStatus() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId("someBatchId")
			.withMessageId(messageId)
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_STATUS_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.extracting(MessageResult::messageId).containsExactly(messageId);

		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getMessageStatusWhenNoHistoryExists() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(any(), any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_STATUS_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getMessageAndDelivery() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId("someBatchId")
			.withMessageId(messageId)
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(any(), any())).thenReturn(List.of(history));

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull().hasSize(1)
			.allSatisfy(hr -> {
				assertThat(hr.messageType()).isEqualTo(SMS);
				assertThat(hr.status()).isEqualTo(SENT);
			});

		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getMessageAndDeliveryWhenNoHistoryExists() {
		// Arrange
		final var messageId = UUID.randomUUID().toString();
		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(any(), any())).thenReturn(emptyList());

		// Act
		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		// Assert and verify
		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getUserBatches() {
		final var municipalityId = "2281";
		final var userId = "userId";
		final var page = 1;
		final var limit = 1;

		final var userBatches = UserBatches.builder()
			.withBatches(List.of(Batch.builder().build()))
			.withMetaData(PagingMetaData.create()
				.withCount(1).withLimit(limit)
				.withPage(page)
				.withTotalPages(15)
				.withTotalRecords(15))
			.build();

		when(mockHistoryService.getUserBatches(MUNICIPALITY_ID, userId, page, limit)).thenReturn(userBatches);

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_BATCHES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", municipalityId, "userId", userId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(UserBatches.class)
			.isEqualTo(userBatches);

		verify(mockHistoryService).getUserBatches(MUNICIPALITY_ID, userId, page, limit);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getUserMessages() {
		final var municipalityId = "2281";
		final var userId = "userId";
		final var page = 1;
		final var limit = 15;

		final var userMessages = createUserMessages();

		when(mockHistoryService.getUserMessages(municipalityId, userId, null, page, limit)).thenReturn(userMessages);

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", municipalityId, "userId", userId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(UserMessages.class)
			.isEqualTo(userMessages);

		verify(mockHistoryService).getUserMessages(municipalityId, userId, null, page, limit);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getBatchUserMessages() {
		final var municipalityId = "2281";
		final var batchId = UUID.randomUUID().toString();
		final var userId = "userId";
		final var page = 1;
		final var limit = 15;

		final var userMessages = createUserMessages();

		when(mockHistoryService.getUserMessages(municipalityId, userId, batchId, page, limit)).thenReturn(userMessages);

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(batchId, page, limit))
				.build(Map.of("municipalityId", municipalityId, "userId", userId)))
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody(UserMessages.class)
			.isEqualTo(userMessages);

		verify(mockHistoryService).getUserMessages(municipalityId, userId, batchId, page, limit);
		verifyNoMoreInteractions(mockHistoryService);
	}

	private static MultiValueMap<String, String> createParameterMap(final Integer page, final Integer limit) {
		return createParameterMap(null, page, limit);
	}

	private static MultiValueMap<String, String> createParameterMap(final String batchId, final Integer page, final Integer limit) {
		final MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		ofNullable(batchId).ifPresent(p -> parameters.add("batchId", p));
		ofNullable(page).ifPresent(p -> parameters.add("page", p.toString()));
		ofNullable(limit).ifPresent(p -> parameters.add("limit", p.toString()));

		return parameters;
	}

	@Test
	void getMessageMetadata() {
		final var messageId = UUID.randomUUID().toString();
		final var history = History.builder()
			.withBatchId("someBatchId")
			.withMessageId(messageId)
			.withDeliveryId("someDeliveryId")
			.withMessageType(DIGITAL_MAIL)
			.withStatus(SENT)
			.build();

		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId)).thenReturn(List.of(history));

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_METADATA_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "messageId", messageId)))
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(HistoryResponse.class);

		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getMessageMetadataWhenNoHistoryExists() {
		final var messageId = UUID.randomUUID().toString();

		when(mockHistoryService.getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId)).thenReturn(emptyList());

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_METADATA_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "messageId", messageId)))
			.exchange()
			.expectStatus().isNotFound()
			.expectBody().isEmpty();

		verify(mockHistoryService).getHistoryByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
		verifyNoMoreInteractions(mockHistoryService);
	}

	@Test
	void getUserMessage() {
		final var messageId = UUID.randomUUID().toString();
		final var userId = "someUser";

		when(mockHistoryService.getUserMessage(MUNICIPALITY_ID, userId, messageId)).thenReturn(UserMessage.builder().build());

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGE_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId, "messageId", messageId)))
			.exchange()
			.expectStatus().isOk()
			.expectBody(UserMessage.class);
	}

	@Test
	void getUserMessageNoMessageExists() {
		final var messageId = UUID.randomUUID().toString();
		final var userId = "someUser";

		when(mockHistoryService.getUserMessage(MUNICIPALITY_ID, userId, messageId))
			.thenThrow(Problem.valueOf(NOT_FOUND, "No message found for message id " + messageId + " and user id " + userId));

		webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGE_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId, "messageId", messageId)))
			.exchange()
			.expectStatus().isNotFound();
	}
}

package se.sundsvall.messaging.api;

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.Constants.BATCH_STATUS_PATH;
import static se.sundsvall.messaging.Constants.CONVERSATION_HISTORY_PATH;
import static se.sundsvall.messaging.Constants.DELIVERY_STATUS_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_METADATA_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_ATTACHMENT_PATH;
import static se.sundsvall.messaging.Constants.MESSAGES_STATUS_PATH;
import static se.sundsvall.messaging.Constants.USER_BATCHES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGE_PATH;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.HistoryService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class HistoryResourceFailureTest {

	@MockitoBean
	private HistoryService mockHistoryService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void getConversationHistoryShouldFailWithInvalidUuId() {
		// Arrange
		final var partyId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(CONVERSATION_HISTORY_PATH).build(Map.of("partyId", partyId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getConversationHistory.partyId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getBatchStatusShouldFailWithInvalidUuId() {
		// Arrange
		final var batchId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(BATCH_STATUS_PATH).build(Map.of("batchId", batchId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getBatchStatus.batchId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getMessageStatusShouldFailWithInvalidUuId() {
		// Arrange
		final var messageId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_STATUS_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMessageStatus.messageId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getDeliveryStatusShouldFailWithInvalidUuId() {
		// Arrange
		final var deliveryId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(DELIVERY_STATUS_PATH).build(Map.of("deliveryId", deliveryId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getDeliveryStatus.deliveryId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getMessageShouldFailWithInvalidUuId() {
		// Arrange
		final var messageId = "not-a-valid-uuid";

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMessage.messageId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserBatches_invalid_municipalityId() {
		final var municipalityId = "not-a-valid-municipalityId";
		final var userId = "userId";
		final var page = 1;
		final var limit = 15;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_BATCHES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", municipalityId, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getUserBatches.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserBatches_to_small_pageAndLimit() {
		final var userId = "userId";
		final var page = 0;
		final var limit = 0;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_BATCHES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("getUserBatches.limit", "must be greater than or equal to 1 and less than or equal to 2147483647"),
				tuple("getUserBatches.page", "must be greater than or equal to 1 and less than or equal to 2147483647"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserBatches_toBigPageAndLimit() {
		final var userId = "userId";
		final var page = Integer.MAX_VALUE + 1;
		final var limit = Integer.MAX_VALUE + 1;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_BATCHES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("getUserBatches.limit", "must be greater than or equal to 1 and less than or equal to 2147483647"),
				tuple("getUserBatches.page", "must be greater than or equal to 1 and less than or equal to 2147483647"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessages_invalid_municipalityId() {
		final var municipalityId = "not-a-valid-municipalityId";
		final var userId = "userId";
		final var page = 1;
		final var limit = 15;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", municipalityId, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getUserMessages.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessages_invalid_batchId() {
		final var batchId = "invalid-uuid";
		final var userId = "userId";
		final var page = 1;
		final var limit = 15;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(batchId, page, limit))
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getUserMessages.batchId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessages_to_small_pageAndLimit() {
		final var userId = "userId";
		final var page = 0;
		final var limit = 0;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("getUserMessages.limit", "must be greater than or equal to 1 and less than or equal to 2147483647"),
				tuple("getUserMessages.page", "must be greater than or equal to 1 and less than or equal to 2147483647"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessages_toBigPageAndLimit() {
		final var userId = "userId";
		final var page = Integer.MAX_VALUE + 1;
		final var limit = Integer.MAX_VALUE + 1;

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(page, limit))
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(
				tuple("getUserMessages.limit", "must be greater than or equal to 1 and less than or equal to 2147483647"),
				tuple("getUserMessages.page", "must be greater than or equal to 1 and less than or equal to 2147483647"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void readAttachment_invalid_municipalityId() {
		final var municipalityId = "not-a-valid-municipalityId";
		final var messageId = UUID.randomUUID().toString();
		final var fileName = "file.txt";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_ATTACHMENT_PATH)
				.build(Map.of("municipalityId", municipalityId, "messageId", messageId, "fileName", fileName)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("readAttachment.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void readAttachment_invalid_messageId() {
		final var messageId = "not-a-valid-messageId";
		final var fileName = "file.txt";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_ATTACHMENT_PATH)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "messageId", messageId, "fileName", fileName)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("readAttachment.messageId", "not a valid UUID"));
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void readAttachment_by_request_parameter_invalid_municipalityId() {
		final var messageId = UUID.randomUUID().toString();
		final var fileName = "file.txt";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/messages/{messageId}/attachments")
				.queryParam("fileName", fileName)
				.build(Map.of("municipalityId", "invalid-municipalityId", "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("readAttachmentByRequestParameter.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void readAttachment_by_request_parameter_invalid_messageId() {
		final var messageId = "not-a-valid-messageId";
		final var fileName = "file.txt";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path("/{municipalityId}/messages/{messageId}/attachments")
				.queryParam("fileName", fileName)
				.build(Map.of("municipalityId", MUNICIPALITY_ID, "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("readAttachmentByRequestParameter.messageId", "not a valid UUID"));
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getMessageMetadata_shouldFailWithInvalidMessageId() {
		final var messageId = "not-valid";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_METADATA_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMessageMetadata.messageId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getMessageMetadata_shouldFailWithInvalidMunicipalityId() {
		final var municipalityId = "not-valid";
		final var messageId = UUID.randomUUID().toString();

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGES_AND_DELIVERY_METADATA_PATH).build(
				Map.of("municipalityId", municipalityId, "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMessageMetadata.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessage_shouldFailWithInvalidMessageId() {
		final var messageId = "not-valid";
		final var userId = "userId";

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGE_PATH).build(
				Map.of("municipalityId", MUNICIPALITY_ID, "userId", userId, "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getUserMessage.messageId", "not a valid UUID"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getUserMessage_shouldFailWithInvalidMunicipalityId() {
		final var municipalityId = "not-valid";
		final var userId = "userId";
		final var messageId = UUID.randomUUID().toString();

		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGE_PATH).build(
				Map.of("municipalityId", municipalityId, "userId", userId, "messageId", messageId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getUserMessage.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockHistoryService);
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
}

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
import static se.sundsvall.messaging.Constants.MESSAGE_AND_DELIVERY_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_ATTACHMENT_PATH;
import static se.sundsvall.messaging.Constants.MESSAGE_STATUS_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class HistoryResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockBean
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
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_STATUS_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
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
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_AND_DELIVERY_PATH).build(Map.of("messageId", messageId, "municipalityId", MUNICIPALITY_ID)))
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
	void getUserMessages_invalid_municipalityId() {
		var municipalityId = "not-a-valid-municipalityId";
		var userId = "userId";
		var page = 1;
		var limit = 15;

		var response = webTestClient.get()
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
	void readAttachment_invalid_municipalityId() {
		var municipalityId = "not-a-valid-municipalityId";
		var messageId = UUID.randomUUID().toString();
		var fileName = "file.txt";

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_ATTACHMENT_PATH)
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
		var municipalityId = "2281";
		var messageId = "not-a-valid-messageId";
		var fileName = "file.txt";

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(MESSAGE_ATTACHMENT_PATH)
				.build(Map.of("municipalityId", municipalityId, "messageId", messageId, "fileName", fileName)))
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

	private MultiValueMap<String, String> createParameterMap(final Integer page, final Integer limit) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		ofNullable(page).ifPresent(p -> parameters.add("page", p.toString()));
		ofNullable(limit).ifPresent(p -> parameters.add("limit", p.toString()));

		return parameters;
	}
}

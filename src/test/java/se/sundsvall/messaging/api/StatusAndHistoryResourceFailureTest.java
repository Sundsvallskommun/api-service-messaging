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
import static se.sundsvall.messaging.Constants.STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH;
import static se.sundsvall.messaging.Constants.STATISTICS_PATH;
import static se.sundsvall.messaging.Constants.USER_MESSAGES_PATH;
import static se.sundsvall.messaging.TestDataFactory.createUserMessagesRequest;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.HistoryService;
import se.sundsvall.messaging.service.StatisticsService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class StatusAndHistoryResourceFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	@MockBean
	private HistoryService mockHistoryService;

	@MockBean
	private StatisticsService mockStatisticsService;

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

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
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

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
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

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
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

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
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

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
	}

	@Test
	void getStatisticsShouldFailWithInvalidMessageType() {
		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(STATISTICS_PATH)
				.queryParam("messageType", "not-valid-type")
				.build(Map.of("municipalityId", MUNICIPALITY_ID)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(Problem.class)
			.returnResult()
			.getResponseBody();

		// Assert and verify
		assertThat(response).isNotNull();
		assertThat(response.getDetail()).isEqualTo("""
			Failed to convert value of type 'java.lang.String' to required type 'se.sundsvall.messaging.model.MessageType'; \
			Failed to convert from type [java.lang.String] to type [@org.springframework.web.bind.annotation.RequestParam \
			@io.swagger.v3.oas.annotations.Parameter se.sundsvall.messaging.model.MessageType] for value [not-valid-type]""");

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
	}

	@ParameterizedTest
	@ValueSource(strings = {" ", " department", "department "})
	void getStatisticsForDepartmentsShouldFailWithInvalidDepartment(final String department) {

		// Act
		final var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(STATISTICS_FOR_SPECIFIC_DEPARTMENT_PATH)
				.build(Map.of("department", department, "municipalityId", MUNICIPALITY_ID)))
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
			.containsExactly(tuple("getDepartmentStatistics.department", "text is not null or not empty or has starting or trailing spaces"));

		verifyNoInteractions(mockHistoryService, mockStatisticsService);
	}

	@Test
	void getUserMessages_invalid_municipalityId() {
		var municipalityId = "not-a-valid-municipalityId";
		var userMessagesRequest = createUserMessagesRequest();

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(userMessagesRequest.getPage(), userMessagesRequest.getLimit(), userMessagesRequest.getUserId()))
				.build(Map.of("municipalityId", municipalityId)))
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
	void getUserMessages_blank_userId() {
		var municipalityId = "2281";
		var userMessagesRequest = createUserMessagesRequest();
		userMessagesRequest.setUserId("");

		var response = webTestClient.get()
			.uri(uriBuilder -> uriBuilder.path(USER_MESSAGES_PATH)
				.queryParams(createParameterMap(userMessagesRequest.getPage(), userMessagesRequest.getLimit(), userMessagesRequest.getUserId()))
				.build(Map.of("municipalityId", municipalityId)))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("userId", "must not be blank"));

		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getAttachment_invalid_municipalityId() {
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
			.containsExactly(tuple("getAttachment.municipalityId", "not a valid municipality ID"));
		verifyNoInteractions(mockHistoryService);
	}

	@Test
	void getAttachment_invalid_messageId() {
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
			.containsExactly(tuple("getAttachment.messageId", "not a valid UUID"));
		verifyNoInteractions(mockHistoryService);
	}

	private MultiValueMap<String, String> createParameterMap(final Integer page, final Integer limit, final String userId) {
		MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();

		ofNullable(page).ifPresent(p -> parameters.add("page", p.toString()));
		ofNullable(limit).ifPresent(p -> parameters.add("limit", p.toString()));
		ofNullable(userId).ifPresent(p -> parameters.add("userId", p));

		return parameters;
	}
}

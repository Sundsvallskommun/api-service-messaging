package se.sundsvall.messaging.api;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsBatchRequest;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.SmsBatchRequest;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceSmsBatchFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String URL = "/" + MUNICIPALITY_ID + "/sms/batch";

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private SmsBatchRequest validRequest;

	@BeforeEach
	void setupRequest() {
		validRequest = createValidSmsBatchRequest();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"ab", "abcdefghijkl"
	})
	void shoudFailWithInvalidSender(String sender) {
		// Arrange
		final var request = validRequest.withSender(sender);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("sender", "size must be between 3 and 11"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void shoudFailWithBlankMessage(String message) {
		// Arrange
		final var request = validRequest.withMessage(message);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactlyInAnyOrder(tuple("message", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shoudFailWithEmptyParties() {
		// Arrange
		final var request = validRequest.withParties(emptyList());

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("parties", "must not be empty"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shoudFailWithInvalidPartyPhoneNumber() {
		// Arrange
		final var request = validRequest
			.withParties(List.of(validRequest.parties().getFirst().withMobileNumber("not-a-valid-number")));

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("parties[0].mobileNumber", "must be a valid MSISDN (example: +46701234567). Regular expression: ^\\+[1-9][\\d]{3,14}$"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

}

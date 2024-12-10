package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceSlackFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String URL = "/" + MUNICIPALITY_ID + "/slack";

	@MockitoBean
	private MessageService messageServiceMock;

	@MockitoBean
	private MessageEventDispatcher eventDispatcherMock;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailForInvalidToken(String token) {
		// Arrange
		final var request = createValidSlackRequest().withToken(token);

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
			.containsExactly(tuple("token", "must not be blank"));

		verifyNoInteractions(messageServiceMock, eventDispatcherMock);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailForInvalidChannel(String channel) {
		// Arrange
		final var request = createValidSlackRequest().withChannel(channel);

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
			.containsExactly(tuple("channel", "must not be blank"));

		verifyNoInteractions(messageServiceMock, eventDispatcherMock);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailForInvalidMessage(String message) {
		// Arrange
		final var request = createValidSlackRequest().withMessage(message);

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
			.containsExactly(tuple("message", "must not be blank"));

		verifyNoInteractions(messageServiceMock, eventDispatcherMock);
	}

}

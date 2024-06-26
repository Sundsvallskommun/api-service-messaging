package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;

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
import se.sundsvall.messaging.api.model.request.MessageRequest;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceMessageFailureTest {
	private static final String URL = "/messages";

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private MessageRequest validRequest;

	@BeforeEach
	void setupRequest() {
		validRequest = MessageRequest.builder().withMessages(List.of(createValidMessageRequestMessage())).build();
	}

	@Test
	void shouldFailWithNullParty() {
		// Arrange
		final var request = validRequest.withMessages(List.of(validRequest.messages().getFirst().withParty(null)));

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
			.containsExactly(tuple("messages[0].party", "must not be null"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = { "", " ", "not-a-uuid" })
	void shouldFailWithInvalidPartyId(String partyId) {
		// Arrange
		final var request = validRequest
			.withMessages(List.of(validRequest.messages().getFirst().withParty(validRequest.messages().getFirst().party().withPartyId(partyId))));

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
			.containsExactly(tuple("messages[0].party.partyId", "not a valid UUID"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceKey(String key) {
		// Arrange
		final var message = validRequest.messages().getFirst();
		final var party = message.party();
		final var externalReference = party.externalReferences().getFirst();
		final var request = validRequest
			.withMessages(List.of(message
				.withParty(party
					.withExternalReferences(List.of(externalReference.withKey(key))))));

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
			.containsExactly(tuple("messages[0].party.externalReferences[0].key", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceValue(String value) {
		// Arrange
		final var message = validRequest.messages().getFirst();
		final var party = message.party();
		final var externalReference = party.externalReferences().getFirst();
		final var request = validRequest
			.withMessages(List.of(message
				.withParty(party
					.withExternalReferences(List.of(externalReference.withValue(value))))));

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
			.containsExactly(tuple("messages[0].party.externalReferences[0].value", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidEmailName(String name) {
		// Arrange
		final var message = validRequest.messages().getFirst();
		final var email = message.sender().email().withName(name);
		final var sender = message.sender().withEmail(email);
		final var request = validRequest.withMessages(List.of(message.withSender(sender)));

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
			.containsExactly(tuple("messages[0].sender.email.name", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = { " ", "not-a-valid-email-address" })
	@NullAndEmptySource
	void shouldFailWithInvalidSender_WhenSenderEmailIsInvalid(String address) {
		final var message = validRequest.messages().getFirst();
		final var email = message.sender().email().withAddress(address);
		final var sender = message.sender().withEmail(email);
		final var request = validRequest.withMessages(List.of(message.withSender(sender)));

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
			.containsAnyOf(
				tuple("messages[0].sender.email.address", "must not be blank"),
				tuple("messages[0].sender.email.address", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = { " ", "not-a-valid-email-address" })
	void shouldFailWithInvalidEmailReplyTo(String replyTo) {
		final var message = validRequest.messages().getFirst();
		final var email = message.sender().email().withReplyTo(replyTo);
		final var sender = message.sender().withEmail(email);
		final var request = validRequest.withMessages(List.of(message.withSender(sender)));

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
			.containsExactly(tuple("messages[0].sender.email.replyTo", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidMessage(String message) {
		// Arrange
		final var request = validRequest.withMessages(List.of(validRequest.messages().getFirst().withMessage(message)));

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
			.containsExactly(tuple("messages[0].message", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}
}

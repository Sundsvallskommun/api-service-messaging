package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import se.sundsvall.messaging.api.model.request.EmailRequest;
import se.sundsvall.messaging.api.model.request.Header;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceEmailFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String URL = "/" + MUNICIPALITY_ID + "/email";

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	private EmailRequest validRequest;

	@Autowired
	private WebTestClient webTestClient;

	@BeforeEach
	void setupRequest() {
		validRequest = createValidEmailRequest();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "not-a-uuid"
	})
	void shouldFailWithInvalidPartyId(String partyId) {
		var request = validRequest.withParty(validRequest.party().withPartyId(partyId));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("party.partyId", "not a valid UUID"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceKey(String key) {
		var externalReference = validRequest.party().externalReferences().getFirst();

		var request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withKey(key))));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("party.externalReferences[0].key", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceValue(String value) {
		var externalReference = validRequest.party().externalReferences().getFirst();

		var request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withValue(value))));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("party.externalReferences[0].value", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"not-an-email-address", " "
	})
	@NullAndEmptySource
	void shouldFailWithNullOrInvalidEmailAddress(String value) {
		var request = validRequest.withEmailAddress(value);

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsAnyOf(
				tuple("emailAddress", "must not be blank"),
				tuple("emailAddress", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		" "
	})
	@NullAndEmptySource
	void shouldFailWithNullOrBlankSubject(String value) {
		var request = validRequest.withSubject(value);

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("subject", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithBlankSenderName(String value) {
		var request = validRequest.withSender(validRequest.sender().withName(value));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("sender.name", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		" ", "not-a-valid-email-address"
	})
	@NullAndEmptySource
	void shouldFailWithBlankOrInvalidSenderAddress(String value) {
		var request = validRequest.withSender(validRequest.sender().withAddress(value));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsAnyOf(
				tuple("sender.address", "must not be blank"),
				tuple("sender.address", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithInvalidReplyToAddress() {
		var request = validRequest.withSender(validRequest.sender().withReplyTo("not-a-valid-email-address"));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("sender.replyTo", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithInvalidHtmlMessage() {
		var request = validRequest.withHtmlMessage("not-a-valid-base-64");

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("htmlMessage", "not a valid BASE64-encoded string"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithMissingFileName(String fileName) {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withName(fileName)));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("attachments[0].name", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithInvalidFileContent() {
		var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withContent("not-a-valid-base-64")));

		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("attachments[0].content", "not a valid BASE64-encoded string"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "abc", "<abc>", "b@c", "<a@b", "a@>"
	})
	void shouldFailWithInvalidHeaderValue(String value) {
		var request = validRequest.withHeaders(Map.of(Header.MESSAGE_ID.getKey(), List.of(value)));

		// Act
		var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("headers.Message-ID", "must start with '<', contain '@' and end with '>'"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}
}

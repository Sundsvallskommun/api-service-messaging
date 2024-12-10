package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.WebMessageRequest;
import se.sundsvall.messaging.api.model.request.WebMessageRequest.Attachment;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceWebMessageFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String URL = "/" + MUNICIPALITY_ID + "/webmessage";

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private WebMessageRequest validRequest;

	@BeforeEach
	void setupRequest() {
		validRequest = createValidWebMessageRequest();
	}

	@Test
	void shouldFailWithNullParty() {
		// Arrange
		final var request = validRequest.withParty(null);

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
			.containsExactly(tuple("party", "must not be null"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		" ", "not-a-uuid"
	})
	@EmptySource
	void shouldFailWithInvalidPartyId(String partyId) {
		// Arrange
		final var request = validRequest.withParty(validRequest.party().withPartyId(partyId));

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
			.containsExactly(tuple("party.partyId", "not a valid UUID"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceKey(String key) {
		// Arrange
		final var externalReference = validRequest.party().externalReferences().getFirst();
		final var request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withKey(key))));

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
			.containsExactly(tuple("party.externalReferences[0].key", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidExternalReferenceValue(String value) {
		// Arrange
		final var externalReference = validRequest.party().externalReferences().getFirst();
		final var request = validRequest.withParty(validRequest.party()
			.withExternalReferences(List.of(externalReference.withValue(value))));

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
			.containsExactly(tuple("party.externalReferences[0].value", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " ", "invalid"
	})
	void shouldFailWithInvalidOepInstance(String oepInstance) {
		// Arrange
		final var request = validRequest.withOepInstance(oepInstance);

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
			.containsExactly(tuple("oepInstance", "instance must be 'internal' or 'external'"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithInvalidMessage(String message) {
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
			.containsExactly(tuple("message", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithToLargeAttachmentList() {
		// Arrange
		final var request = validRequest.withAttachments(createAttachments(11));

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
			.containsExactly(tuple("attachments", "size must be between 0 and 10"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	private List<Attachment> createAttachments(int size) {
		final List<Attachment> list = new ArrayList<>();
		for (var i = 0; i < size; i++) {
			list.add(WebMessageRequest.Attachment.builder()
				.withFileName("someFileName")
				.withMimeType("text/plain")
				.withBase64Data("bG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQK")
				.build());
		}

		return list;
	}

}

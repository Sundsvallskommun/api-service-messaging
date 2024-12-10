package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;

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
import se.sundsvall.messaging.api.model.request.DigitalMailRequest;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceDigitalMailFailureTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String URL = "/" + MUNICIPALITY_ID + "/digital-mail";

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	private DigitalMailRequest validRequest;

	@BeforeEach
	void setupRequest() {
		validRequest = createValidDigitalMailRequest();
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
		"", " ", "not-a-uuid"
	})
	void shouldFailWithInvalidPartyId(String partyId) {
		// Arrange
		final var request = validRequest.withParty(validRequest.party().withPartyIds(List.of(partyId)));

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
			.containsExactly(tuple("party.partyIds[0]", "not a valid UUID"));

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

	@Test
	void shouldFailWithNullSenderSupportInfo() {
		// Arrange
		final var request = validRequest.withSender(validRequest.sender().withSupportInfo(null));

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
			.containsExactly(tuple("sender.supportInfo", "must not be null"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithSupportInfoWithInvalidText(String text) {
		// Arrange
		final var request = validRequest.withSender(validRequest.sender()
			.withSupportInfo(validRequest.sender().supportInfo().withText(text)));

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
			.containsExactly(tuple("sender.supportInfo.text", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithSupportInfoWithInvalidEmailAddress() {
		// Arrange
		final var request = validRequest.withSender(validRequest.sender()
			.withSupportInfo(validRequest.sender().supportInfo().withEmailAddress("not-an-email-address")));

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
			.containsExactly(tuple("sender.supportInfo.emailAddress", "must be a well-formed email address"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = {
		" ", "not-valid-content-type"
	})
	@NullAndEmptySource
	void shouldFailWithInvalidContentType(final String contentType) {
		// Arrange
		final var request = validRequest.withContentType(contentType);

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
				tuple("contentType", "must not be blank"),
				tuple("contentType", "must be one of: [text/plain, text/html]"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithMissingBody(String body) {
		// Arrange
		final var request = validRequest.withBody(body);

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
			.containsExactly(tuple("body", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);

	}

	@ParameterizedTest
	@ValueSource(strings = {
		" ", "not-valid-content-type"
	})
	@NullAndEmptySource
	void shouldFailWithFileWithInvalidContentType(final String contentType) {
		// Arrange
		final var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withContentType(contentType)));

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
			.containsExactly(tuple("attachments[0].contentType", "must be one of: [application/pdf]"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithFileWithInvalidContent(String content) {
		// Arrange
		final var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withContent(content)));

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
			.containsExactly(tuple("attachments[0].content", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(strings = " ")
	@NullAndEmptySource
	void shouldFailWithFileWithInvalidName(String fileName) {
		// Arrange
		final var request = validRequest.withAttachments(List.of(validRequest.attachments().getFirst().withFilename(fileName)));

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
			.containsExactly(tuple("attachments[0].filename", "must not be blank"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

}

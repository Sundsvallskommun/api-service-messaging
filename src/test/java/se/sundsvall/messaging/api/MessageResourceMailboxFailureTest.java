package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.ORGANIZATION_NUMBER;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import org.zalando.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceMailboxFailureTest {

	private static final String URL = "/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/mailboxes";

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest(name = "{0}")
	@MethodSource("faultyPartyProvider")
	void shouldFailWithFaultyParty(String testName, List<String> partyIds) {
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(partyIds)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMailboxes.partyIds[0].<list element>", "not a valid UUID"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	public static Stream<Arguments> faultyPartyProvider() {
		var validPartyId = "550e8400-e29b-41d4-a716-446655440000";
		return Stream.of(
			Arguments.of("empty partyId", List.of("")),
			Arguments.of("whitespace partyId", List.of(" ")),
			Arguments.of("invalid partyId", List.of("invalid-party-id")),
			Arguments.of("one valid, one invalid", List.of("invalid-party-id", validPartyId)));
	}

	@Test
	void shouldFailWithNonUniquePartyIds() {
		final var partyId = UUID.randomUUID().toString();
		final var response = webTestClient.post()
			.uri(URL)
			.contentType(APPLICATION_JSON)
			.bodyValue(List.of(partyId, partyId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMailboxes.partyIds", "must only contain unique elements"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithFaultyOrganizationNumber() {
		final var partyIds = List.of(UUID.randomUUID().toString());
		final var response = webTestClient.post()
			.uri(URL.replace(ORGANIZATION_NUMBER, "invalid"))
			.contentType(APPLICATION_JSON)
			.bodyValue(partyIds)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMailboxes.organizationNumber", "must match the regular expression ^([1235789][\\d][2-9]\\d{7})$"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}

	@Test
	void shouldFailWithFaultyMunicipalityId() {
		final var partyIds = List.of(UUID.randomUUID().toString());
		final var response = webTestClient.post()
			.uri(URL.replace(MUNICIPALITY_ID, "invalid"))
			.contentType(APPLICATION_JSON)
			.bodyValue(partyIds)
			.exchange()
			.expectStatus().isBadRequest()
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::getField, Violation::getMessage)
			.containsExactly(tuple("getMailboxes.municipalityId", "not a valid municipality ID"));

		verifyNoInteractions(mockMessageService, mockEventDispatcher);
	}
}

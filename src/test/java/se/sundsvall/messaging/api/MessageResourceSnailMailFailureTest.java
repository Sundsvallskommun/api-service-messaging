package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.problem.violations.Violation;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.SnailMailRequest;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class MessageResourceSnailMailFailureTest {

	@MockitoBean
	private MessageService mockMessageService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void ensureNoInteractions() {
		verifyNoInteractions(mockMessageService);
	}

	@Test
	void shouldFailWithInvalidMunicipalityId() {
		var validRequest = createValidSnailMailRequest();
		var response = webTestClient.post()
			.uri("/invalid-municipalityId/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("addSnailMailToBatch.municipalityId", "not a valid municipality ID"));

	}

	@Test
	void shouldFailWithInvalidBatchId() {
		var validRequest = createValidSnailMailRequest();
		var response = webTestClient.post()
			.uri("/2281/snail-mail?batchId=invalid-batchId")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("addSnailMailToBatch.batchId", "not a valid UUID"));

	}

	@Test
	void shouldFailWithEmptyRequestBody() {
		var emptyBody = SnailMailRequest.builder().build();
		var response = webTestClient.post()
			.uri("/2281/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(emptyBody)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("department", "must not be blank"));
	}

	@Test
	void shouldFailWithInvalidFolderName() {
		var invalidRequest = createValidSnailMailRequest()
			.withFolderName("questionMarkIsInvalid_?");

		var response = webTestClient.post()
			.uri("/2281/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalidRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_PROBLEM_JSON)
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getViolations())
			.extracting(Violation::field, Violation::message)
			.containsExactly(tuple("folderName", "not a valid folder name"));
	}

}

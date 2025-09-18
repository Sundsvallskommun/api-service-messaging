package se.sundsvall.messaging.api;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceSnailMailFailureTest {

	@MockitoBean
	private MessageService mockMessageService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void shouldFailWithInvalidMunicipalityId() {
		var validRequest = createValidSnailMailRequest();
		webTestClient.post()
			.uri("/invalid-municipalityId/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void shouldFailWithInvalidBatchId() {
		var validRequest = createValidSnailMailRequest();
		webTestClient.post()
			.uri("/2281/snail-mail?batchId=invalid-batchId")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void shouldFailWithInvalidRequestBody() {
		var invalidRequest = createValidSnailMailRequest().withDepartment(null);
		webTestClient.post()
			.uri("/2281/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(invalidRequest)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
	}

}

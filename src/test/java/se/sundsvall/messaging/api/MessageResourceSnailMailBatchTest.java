package se.sundsvall.messaging.api;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

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
class MessageResourceSnailMailBatchTest {

	@MockitoBean
	private MessageService mockMessageService;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void shouldReturnOK() {
		webTestClient.post()
			.uri("/2281/snail-mail/batch/f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(HttpStatus.NOT_IMPLEMENTED);
	}

}

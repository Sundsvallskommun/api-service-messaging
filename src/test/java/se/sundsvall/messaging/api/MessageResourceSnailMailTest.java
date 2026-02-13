package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class MessageResourceSnailMailTest {

	@MockitoBean
	private MessageService mockMessageService;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void ensureNoUnexpectedInteractions() {
		verifyNoMoreInteractions(mockMessageService);
	}

	@Test
	void sendSnailMail_shouldReturnCreated() {
		var validRequest = createValidSnailMailRequest();

		when(mockMessageService.sendSnailMail(any(), any()))
			.thenReturn(new InternalDeliveryResult("messageId", "deliveryId", MessageType.SNAIL_MAIL,
				MessageStatus.SENT, MUNICIPALITY_ID));

		var response = webTestClient.post()
			.uri("/2281/snail-mail?batchId=f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(validRequest)
			.header(Identifier.HEADER_NAME, "test01user; type=adAccount")
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/messages/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("messageId");
		assertThat(response.deliveries()).allSatisfy(delivery -> {
			assertThat(delivery).isNotNull();
			assertThat(delivery.deliveryId()).isEqualTo("deliveryId");
			assertThat(delivery.messageType()).isEqualTo(MessageType.SNAIL_MAIL);
			assertThat(delivery.status()).isEqualTo(MessageStatus.SENT);
		});
		verify(mockMessageService).sendSnailMail(any(), any());
	}

	@Test
	void triggerSnailMailBatch_shouldReturnOK() {
		webTestClient.post()
			.uri("/2281/snail-mail/batch/f427952b-247c-4d3b-b081-675a467b3619")
			.contentType(MediaType.APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk();

		verify(mockMessageService).sendSnailMailBatch("2281", "f427952b-247c-4d3b-b081-675a467b3619");
	}

}

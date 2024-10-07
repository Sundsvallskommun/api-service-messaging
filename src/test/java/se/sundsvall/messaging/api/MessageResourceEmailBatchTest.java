package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;

import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;
import se.sundsvall.messaging.test.annotation.UnitTest;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@UnitTest
class MessageResourceEmailBatchTest {

	private static final String MUNICIPALITY_ID = "2281";
	private static final String URL = "/" + MUNICIPALITY_ID + "/email/batch";
	private static final String ORIGIN_HEADER = "x-origin";
	private static final String ORIGIN = "origin";
	private static final String ORIGIN_ISSUER = "x-issuer";
	private static final String ISSUER = "issuer";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(EMAIL)
		.withStatus(SENT)
		.build();

	private static final InternalDeliveryBatchResult DELIVERY_BATCH_RESULT = InternalDeliveryBatchResult.builder()
		.withBatchId("someBatchId")
		.withDeliveries(List.of(DELIVERY_RESULT))
		.withMunicipalityId(MUNICIPALITY_ID)
		.build();

	@MockBean
	private MessageService mockMessageService;

	@MockBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	void sendBatch(boolean includeOptionalHeaders) {
		// Arrange

		final var request = createValidEmailBatchRequest();

		when(mockEventDispatcher.handleEmailBatchRequest(any(), any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/2281/status/batch/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageBatchResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.batchId()).isEqualTo("someBatchId");
		assertThat(response.messages()).isNotNull().hasSize(1);
		assertThat(response.messages().getFirst().messageId()).isEqualTo("someMessageId");
		assertThat(response.messages().getFirst().deliveries()).isNotNull().hasSize(1);
		assertThat(response.messages().getFirst().deliveries().getFirst().messageType()).isEqualTo(EMAIL);
		assertThat(response.messages().getFirst().deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.messages().getFirst().deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleEmailBatchRequest(includeOptionalHeaders ? request.withOrigin(ORIGIN) : request, MUNICIPALITY_ID);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Consumer<HttpHeaders> handleHeaders(boolean includeOptionalHeaders) {
		return httpHeaders -> {
			if (includeOptionalHeaders) {
				httpHeaders.add(ORIGIN_HEADER, ORIGIN);
				httpHeaders.add(ORIGIN_ISSUER, ISSUER);
			}
		};
	}
}

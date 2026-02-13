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
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.X_ORIGIN_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_ORIGIN_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_USER_NAME;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailBatchRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.EMAIL;

import java.util.List;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.EmailBatchRequest;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class MessageResourceEmailBatchTest {

	private static final String URL = "/" + MUNICIPALITY_ID + "/email/batch";

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

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void sendBatch(boolean includeOptionalHeaders) {
		// Arrange
		final var request = createValidEmailBatchRequest();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);

		when(mockEventDispatcher.handleEmailBatchRequest(any())).thenReturn(DELIVERY_BATCH_RESULT);

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

		verify(mockEventDispatcher).handleEmailBatchRequest(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	@Test
	void testOldHeadersShouldBePreserved() {
		// Arrange
		final var request = createValidEmailBatchRequest();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);

		when(mockEventDispatcher.handleEmailBatchRequest(any())).thenReturn(DELIVERY_BATCH_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(oldHeaders())
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

		verify(mockEventDispatcher).handleEmailBatchRequest(decoratedRequest.withOrigin(X_ORIGIN_HEADER_VALUE).withIssuer(X_ISSUER_HEADER_VALUE));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	private static Consumer<HttpHeaders> handleHeaders(boolean includeOptionalHeaders) {
		return httpHeaders -> {
			if (includeOptionalHeaders) {
				httpHeaders.add(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE);
				httpHeaders.add(X_ISSUER_HEADER, X_ISSUER_HEADER_VALUE);
				httpHeaders.add(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE);
			}
		};
	}

	private static Consumer<HttpHeaders> oldHeaders() {
		return httpHeaders -> {
			httpHeaders.add(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE);
			httpHeaders.add(X_ISSUER_HEADER, X_ISSUER_HEADER_VALUE);
		};
	}

	private static EmailBatchRequest addHeaderValues(EmailBatchRequest request) {
		return request.withOrigin(X_ORIGIN_HEADER_VALUE)
			.withIssuer(X_SENT_BY_HEADER_USER_NAME);
	}
}

package se.sundsvall.messaging.api;

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.request.DigitalInvoiceRequest;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

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
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalInvoiceRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_INVOICE;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceDigitalInvoiceTest {

	private static final String URL = "/" + MUNICIPALITY_ID + "/digital-invoice";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMessageType(DIGITAL_INVOICE)
		.withMunicipalityId(MUNICIPALITY_ID)
		.withStatus(SENT)
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
	void sendSynchronous(boolean includeOptionalHeaders) {
		// Arrange
		final var request = createValidDigitalInvoiceRequest();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendDigitalInvoice(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/messages/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("someMessageId");
		assertThat(response.deliveries()).isNotNull().hasSize(1);
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_INVOICE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendDigitalInvoice(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
	}

	@ParameterizedTest
	@ValueSource(booleans = {
		true, false
	})
	void sendAsynchronous(boolean includeOptionalHeaders) {
		// Arrange
		final var request = createValidDigitalInvoiceRequest();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockEventDispatcher.handleDigitalInvoiceRequest(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL + "?async=true")
			.headers(handleHeaders(includeOptionalHeaders))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/messages/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("someMessageId");
		assertThat(response.deliveries()).isNotNull().hasSize(1);
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_INVOICE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockEventDispatcher).handleDigitalInvoiceRequest(includeOptionalHeaders ? addHeaderValues(decoratedRequest) : decoratedRequest);
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockMessageService);
	}

	@Test
	void testOldHeaderShouldBePreserved() {
		// Arrange
		final var request = createValidDigitalInvoiceRequest();
		final var decoratedRequest = request.withMunicipalityId(MUNICIPALITY_ID);
		when(mockMessageService.sendDigitalInvoice(any())).thenReturn(DELIVERY_RESULT);

		// Act
		final var response = webTestClient.post()
			.uri(URL)
			.headers(oldHeaders())
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectHeader().exists(LOCATION)
			.expectHeader().valuesMatch(LOCATION, "^/" + MUNICIPALITY_ID + "/status/messages/(.*)$")
			.expectStatus().isCreated()
			.expectBody(MessageResult.class)
			.returnResult()
			.getResponseBody();

		// Assert & verify
		assertThat(response).isNotNull();
		assertThat(response.messageId()).isEqualTo("someMessageId");
		assertThat(response.deliveries()).isNotNull().hasSize(1);
		assertThat(response.deliveries().getFirst().messageType()).isEqualTo(DIGITAL_INVOICE);
		assertThat(response.deliveries().getFirst().deliveryId()).isEqualTo("someDeliveryId");
		assertThat(response.deliveries().getFirst().status()).isEqualTo(SENT);

		verify(mockMessageService).sendDigitalInvoice(decoratedRequest.withOrigin(X_ORIGIN_HEADER_VALUE).withIssuer(X_ISSUER_HEADER_VALUE));
		verifyNoMoreInteractions(mockEventDispatcher);
		verifyNoInteractions(mockEventDispatcher);
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

	private static DigitalInvoiceRequest addHeaderValues(DigitalInvoiceRequest request) {
		return request.withOrigin(X_ORIGIN_HEADER_VALUE)
			.withIssuer(X_SENT_BY_HEADER_USER_NAME);
	}
}

package se.sundsvall.messaging.api.interceptor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.ORGANIZATION_NUMBER;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_ISSUER_HEADER_VALUE;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryBatchResult;
import se.sundsvall.messaging.model.InternalDeliveryResult;
import se.sundsvall.messaging.service.MessageService;

/**
 * Verify that the HeaderInterceptor works and only that.
 */
@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class HeaderInterceptorResourceTest {

	private static final String DIGITAL_MAIL_URL = "/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/digital-mail";

	private static final InternalDeliveryResult DELIVERY_RESULT = InternalDeliveryResult.builder()
		.withMessageId("someMessageId")
		.withDeliveryId("someDeliveryId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withMessageType(DIGITAL_MAIL)
		.withStatus(SENT)
		.build();

	private static final InternalDeliveryBatchResult DELIVERY_BATCH_RESULT = InternalDeliveryBatchResult.builder()
		.withBatchId("someBatchId")
		.withMunicipalityId(MUNICIPALITY_ID)
		.withDeliveries(List.of(DELIVERY_RESULT))
		.build();

	@MockitoBean
	private MessageService mockMessageService;

	@Autowired
	private WebTestClient webTestClient;

	// This resource doesn't consume the issuer header but the interceptor should translate it to a X-Sent-By header,
	// which will be added to the "issuer" parameter inside the decorated DigitalMailRequest.
	@Test
	void testIssuerHeaderShouldBeTranslatedForDigitalMail() {
		final var request = createValidDigitalMailRequest();
		final var decoratedRequest = request.withIssuer(X_ISSUER_HEADER_VALUE).withMunicipalityId(MUNICIPALITY_ID);

		when(mockMessageService.sendLetter(any(), anyString())).thenReturn(DELIVERY_BATCH_RESULT);

		webTestClient.post()
			.uri(DIGITAL_MAIL_URL)
			.headers(headers -> headers.add(X_ISSUER_HEADER, X_ISSUER_HEADER_VALUE))
			.contentType(APPLICATION_JSON)
			.bodyValue(request)
			.exchange()
			.expectBody(MessageBatchResult.class)
			.returnResult();

		verify(mockMessageService).sendDigitalMail(decoratedRequest, ORGANIZATION_NUMBER);
		verifyNoMoreInteractions(mockMessageService);
	}
}

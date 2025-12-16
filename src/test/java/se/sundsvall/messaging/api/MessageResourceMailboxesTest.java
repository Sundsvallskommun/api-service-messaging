package se.sundsvall.messaging.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static se.sundsvall.messaging.TestDataFactory.MUNICIPALITY_ID;
import static se.sundsvall.messaging.TestDataFactory.ORGANIZATION_NUMBER;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER;
import static se.sundsvall.messaging.TestDataFactory.X_SENT_BY_HEADER_VALUE;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.Mailbox;
import se.sundsvall.messaging.service.MessageEventDispatcher;
import se.sundsvall.messaging.service.MessageService;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class MessageResourceMailboxesTest {

	private static final String MAILBOXES_URL = "/" + MUNICIPALITY_ID + "/" + ORGANIZATION_NUMBER + "/mailboxes";

	@MockitoBean
	private MessageService mockMessageService;

	@MockitoBean
	private MessageEventDispatcher mockEventDispatcher;

	@Autowired
	private WebTestClient webTestClient;

	@Test
	void testGetMailboxes() {
		final var uuid = UUID.randomUUID().toString();
		final var uuid2 = UUID.randomUUID().toString();
		final var partyIds = List.of(uuid, uuid2);

		when(mockMessageService.getMailboxes(MUNICIPALITY_ID, ORGANIZATION_NUMBER, partyIds))
			.thenReturn(List.of(new Mailbox(uuid, null, "Kivra", true), new Mailbox(uuid2, "someReason", "Kivra", false)));

		final var response = webTestClient.post()
			.uri(MAILBOXES_URL)
			.headers(xSentByHeader())
			.contentType(APPLICATION_JSON)
			.bodyValue(partyIds)
			.exchange()
			.expectStatus().isOk()
			.expectBodyList(Mailbox.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response).extracting(Mailbox::partyId, Mailbox::supplier, Mailbox::reachable, Mailbox::reason)
			.containsExactlyInAnyOrder(
				tuple(uuid, "Kivra", true, null),
				tuple(uuid2, "Kivra", false, "someReason"));

		verify(mockMessageService).getMailboxes(MUNICIPALITY_ID, ORGANIZATION_NUMBER, partyIds);
		verifyNoMoreInteractions(mockMessageService);
		verifyNoInteractions(mockEventDispatcher);
	}

	private static Consumer<HttpHeaders> xSentByHeader() {
		return httpHeaders -> httpHeaders.add(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE);
	}
}

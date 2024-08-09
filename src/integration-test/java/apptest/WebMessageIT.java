package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/WebMessageIT/", classes = Application.class)
class WebMessageIT extends AbstractMessagingAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/webmessage";

	private static final String REQUEST_FILE = "request.json";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/message/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageResult.class);

		final var messageId = response.messageId();

		// Make sure we received a message id as a proper UUID
		assertValidUuid(messageId);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {

				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull()
					.isNotEmpty()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
					});

				return true;
			});
	}

	@Test
	void test2_internalServerErrorFromWebMessageSender() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY)
			.sendRequestAndVerifyResponse();
	}

}

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
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;

@WireMockAppTestSuite(files = "classpath:/DigitalMailDeprecatedIT/", classes = Application.class)
class DigitalMailDeprecatedIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/digital-mail";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE)
			.withHeader(X_SENT_BY_HEADER, X_SENT_BY_HEADER_VALUE)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);

		final var messageId = response.messages().getFirst().messageId();
		final var batchId = response.batchId();

		// Make sure we received a message id and a batch id as proper UUID:s
		assertValidUuid(messageId);
		assertValidUuid(batchId);

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
						assertValidUuid(historyEntry.getBatchId());
						assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isEqualTo(X_ORIGIN_HEADER_VALUE);
						assertThat(historyEntry.getIssuer()).isEqualTo(X_SENT_BY_HEADER_USER_NAME);
						assertThat(historyEntry.getOrganizationNumber()).isEqualTo(ORGANIZATION_NUMBER);
					});

				return true;
			});
	}

	@Test
	void test2_internalServerErrorFromDigitalMailSender() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY)
			.sendRequestAndVerifyResponse();
	}

}

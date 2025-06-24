package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;

@WireMockAppTestSuite(files = "classpath:/EmailBatchIT/", classes = Application.class)
class EmailBatchIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/email/batch";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test01_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_SENDER, SENDER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {

				response.messages().stream()
					.map(MessageResult::messageId)
					.forEach(messageId -> {
						assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
						assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
							.isNotEmpty()
							.allSatisfy(historyEntry -> {
								assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
								assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
								assertThat(historyEntry.getStatus()).isEqualTo(SENT);
								assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
								assertThat(historyEntry.getIssuer()).isEqualTo(SENDER_VALUE);
							});
					});

				return true;
			});
	}

	@Test
	void test02_internalServerErrorsFromEmailSender() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_SENDER, SENDER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.withExpectedResponse(RESPONSE_FILE)
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {

				response.messages().stream()
					.map(MessageResult::messageId)
					.forEach(messageId -> {
						assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
						assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
							.isNotEmpty()
							.allSatisfy(historyEntry -> {
								assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
								assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
								assertThat(historyEntry.getStatus()).isEqualTo(FAILED);
								assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
								assertThat(historyEntry.getIssuer()).isEqualTo(SENDER_VALUE);
							});
					});

				return true;
			});
	}
}

package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/EmailBatchIT/", classes = Application.class)
class EmailBatchIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/email/batch";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test01_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		var batchId = response.batchId();

		response.messages().stream()
			.map(MessageResult::messageId)
			.forEach(messageId -> {
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				assertThat(historyRepository.findByMessageId(messageId))
					.isNotEmpty()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
					});
			});
	}

	@Test
	void test02_internalServerErrorsFromEmailSender() throws Exception {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.withExpectedResponse("response.json")
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);
	}
}

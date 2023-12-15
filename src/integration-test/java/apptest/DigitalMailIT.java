package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/DigitalMailIT/", classes = Application.class)
class DigitalMailIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/digital-mail";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);

		final var messageId = response.messages().get(0).messageId();
		final var batchId = response.batchId();

		// Make sure we received a message id and a batch id as proper UUID:s
		assertValidUuid(messageId);
		assertValidUuid(batchId);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull()
			.isNotEmpty()
			.allSatisfy(historyEntry -> {
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
			});
	}

	@Test
	void test2_internalServerErrorFromDigitalMailSender() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY)
			.sendRequestAndVerifyResponse();
	}
}

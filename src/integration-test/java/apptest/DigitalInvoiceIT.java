package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/DigitalInvoiceIT/", classes = Application.class)
class DigitalInvoiceIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/digital-invoice";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/message/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageResult.class);

		final var messageId = response.messageId();

		// Make sure we received a message id as a proper UUID
		assertValidUuid(messageId);

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

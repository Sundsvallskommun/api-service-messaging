package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.sundsvall.dept44.support.Identifier;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;

@WireMockAppTestSuite(files = "classpath:/SnailMailIT/", classes = Application.class)
class SnailMailIT extends AbstractMessagingAppTest {

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_sendSnailMail_successfulRequest() throws JsonProcessingException, ClassNotFoundException {
		final var response = setupCall()
			.withServicePath("/2281/snail-mail?batchId=ebaa0101-b68d-4736-9bdb-a99d74c27a71")
			.withHeader(Identifier.HEADER_NAME, X_SENT_BY_HEADER_VALUE)
			.withHeader(X_ORIGIN_HEADER, X_ORIGIN_HEADER_VALUE)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/messages/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageResult.class);

		final var messageId = response.messageId();
		final var deliveryId = response.deliveries().getFirst().deliveryId();

		// Make sure we received a message id and a batch id as proper UUID:s
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

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
					});

				return true;
			});
	}

	@Test
	void test2_triggerSnailMailBatch_successfulRequest() {
		setupCall()
			.withServicePath("/2281/snail-mail/batch/ebaa0101-b68d-4736-9bdb-a99d74c27a71")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(OK)
			.sendRequestAndVerifyResponse();
	}

}

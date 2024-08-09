package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/LetterIT/", classes = Application.class)
class LetterIT extends AbstractMessagingAppTest {

	private static final String MUNICIPALITY_ID = "2281";

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/letter";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequestByDigital() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);

		final var batchId = response.batchId();
		final var messageId = response.messages().getFirst().messageId();
		final var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				final var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				// We should have a single history entry
				assertThat(history).hasSize(1);

				final var historyEntry = history.getFirst();
				assertThat(historyEntry.getBatchId()).isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
				assertThat(historyEntry.getDeliveryId()).isEqualTo(deliveryId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
				assertThat(historyEntry.getMessageType()).isEqualTo(DIGITAL_MAIL);
				assertThat(historyEntry.getOriginalMessageType()).isEqualTo(LETTER);

				return true;
			});
	}

	@Test
	void test2_ErrorFromDigital_SuccessfulSnailMail() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		assertThat(response.messages()).hasSize(1);

		final var batchId = response.batchId();
		final var messageId = response.messages().getFirst().messageId();
		final var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

				final var history = historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId);
				// We should have three history entries
				assertThat(history).hasSize(2);
				// The batch id should be the same for everything in the history
				assertThat(history).extracting(HistoryEntity::getBatchId).containsOnly(batchId);
				// The message id should be the same for everything in the history
				assertThat(history).extracting(HistoryEntity::getMessageId).containsOnly(messageId);
				assertThat(history).extracting(HistoryEntity::getDeliveryId).contains(deliveryId);
				assertThat(history).extracting(HistoryEntity::getStatus)
					.containsExactlyInAnyOrder(SENT, FAILED);
				assertThat(history).extracting(HistoryEntity::getMessageType)
					.containsExactlyInAnyOrder(DIGITAL_MAIL, SNAIL_MAIL);
				assertThat(history).extracting(HistoryEntity::getOriginalMessageType).containsOnly(LETTER);

				return true;
			});
	}

	@Test
	void test3_ErrorFromDigital_ErrorFromSnailMail() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY);
	}

	// TODO: create an additional app-test that tests the "ANY" delivery mode stuff...
}

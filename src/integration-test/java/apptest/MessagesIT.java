package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
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

@WireMockAppTestSuite(files = "classpath:/MessagesIT/", classes = Application.class)
class MessagesIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/messages";
	private static final String BATCH_ID = "batchId";
	private static final String MESSAGE_ID = "messageId";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest_bySms() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_SENDER, SENDER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		final var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
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
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).as(BATCH_ID).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).as(MESSAGE_ID).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
						assertThat(historyEntry.getIssuer()).isEqualTo(SENDER_VALUE);
					});

				return true;
			});
	}

	@Test
	void test2_successfulRequest_byEmail() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		final var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
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
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).as(BATCH_ID).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).as(MESSAGE_ID).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isNull();
						assertThat(historyEntry.getIssuer()).isNull();
					});

				return true;
			});
	}

	@Test
	void test3_noContactSettingsFound() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_SENDER, SENDER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		final var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
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
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).as(BATCH_ID).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).as(MESSAGE_ID).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(NO_CONTACT_SETTINGS_FOUND);
						assertThat(historyEntry.getOrigin()).isNull();
						assertThat(historyEntry.getIssuer()).isEqualTo(SENDER_VALUE);
					});

				return true;
			});
	}

	@Test
	void test4_noContactWanted() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		final var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
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
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull().allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).as(BATCH_ID).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).as(MESSAGE_ID).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(NO_CONTACT_WANTED);
						assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
						assertThat(historyEntry.getIssuer()).isNull();
					});

				return true;
			});
	}

	@Test
	void test5_successfulRequest_byBothSmsAndEmail() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_SENDER, SENDER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		final var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(2);
		final var deliveryId1 = response.messages().getFirst().deliveries().getFirst().deliveryId();
		final var deliveryId2 = response.messages().getFirst().deliveries().get(1).deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId1);
		assertValidUuid(deliveryId2);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {

				// Make sure that there doesn't exist a message entity
				assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndMessageId(MUNICIPALITY_ID, messageId))
					.isNotNull()
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getBatchId()).as(BATCH_ID).isEqualTo(batchId);
						assertThat(historyEntry.getMessageId()).as(MESSAGE_ID).isEqualTo(messageId);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
						assertThat(historyEntry.getIssuer()).isEqualTo(SENDER_VALUE);
					});

				return true;
			});
	}
}

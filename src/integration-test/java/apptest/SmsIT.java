package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

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
import se.sundsvall.messaging.integration.db.entity.HistoryEntity;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/SmsIT/", classes = Application.class)
class SmsIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/" + MUNICIPALITY_ID + "/sms";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/message/(.*)$"))
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
						assertThat(historyEntry.getMessageId()).isEqualTo(response.messageId());
						assertThat(historyEntry.getMessageType()).isEqualTo(SMS);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
						assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
					});

				return true;
			});
	}

	@Test
	void test2_internalServerErrorFromSmsSender() {
		setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(BAD_GATEWAY)
			.sendRequestAndVerifyResponse();
	}

	@Test
	void test3_successfulBatchRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH + "/batch")
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();
		final var messageIds = response.messages().stream().map(MessageResult::messageId).toList();

		// Make sure we received a batch id and message ids as a proper UUID
		assertValidUuid(batchId);
		messageIds.forEach(this::assertValidUuid);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {

				// Make sure that there doesn't exist any message entities
				messageIds.forEach(messageId -> {
					assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				});

				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndBatchId(MUNICIPALITY_ID, batchId))
					.isNotNull()
					.hasSize(2)
					.extracting(HistoryEntity::getMessageType, HistoryEntity::getStatus, HistoryEntity::getOrigin, HistoryEntity::getIssuer)
					.containsExactlyInAnyOrder(
						tuple(SMS, SENT, ORIGIN, ISSUER),
						tuple(SMS, SENT, ORIGIN, ISSUER));

				return true;
			});
	}

	@Test
	void test4_internalServerErrorFromSmsSenderOnBatch() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH + "/batch")
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();
		final var messageIds = response.messages().stream().map(MessageResult::messageId).toList();

		// Make sure we received a batch id and message ids as a proper UUID
		assertValidUuid(batchId);
		messageIds.forEach(this::assertValidUuid);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist any message entities
				messageIds.forEach(messageId -> {
					assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				});

				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndBatchId(MUNICIPALITY_ID, batchId))
					.isNotNull()
					.hasSize(2)
					.extracting(HistoryEntity::getMessageType, HistoryEntity::getStatus, HistoryEntity::getOrigin, HistoryEntity::getIssuer)
					.containsExactlyInAnyOrder(
						tuple(SMS, SENT, ORIGIN, null),
						tuple(SMS, FAILED, ORIGIN, null));

				return true;
			});
	}

	@Test
	void test5_successfulHighPriorityRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/message/(.*)$"))
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
						assertThat(historyEntry.getMessageId()).isEqualTo(response.messageId());
						assertThat(historyEntry.getMessageType()).isEqualTo(SMS);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isNull();
						assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
					});

				return true;
			});
	}

	@Test
	void test6_successfulHighPriorityBatchRequest() throws Exception {
		final var response = setupCall()
			.withServicePath(SERVICE_PATH + "/batch")
			.withHeader(HEADER_ORIGIN, ORIGIN)
			.withHeader(HEADER_ISSUER, ISSUER)
			.withRequest(REQUEST_FILE)
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/" + MUNICIPALITY_ID + "/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		final var batchId = response.batchId();
		final var messageIds = response.messages().stream().map(MessageResult::messageId).toList();

		// Make sure we received a batch id and message ids as a proper UUID
		assertValidUuid(batchId);
		messageIds.forEach(this::assertValidUuid);

		await()
			.atMost(10, TimeUnit.SECONDS)
			.until(() -> {
				// Make sure that there doesn't exist any message entities
				messageIds.forEach(messageId -> {
					assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
				});

				// Make sure that there exists a history entry with the correct id and status
				assertThat(historyRepository.findByMunicipalityIdAndBatchId(MUNICIPALITY_ID, batchId))
					.isNotNull()
					.hasSize(2)
					.allSatisfy(historyEntry -> {
						assertThat(historyEntry.getMessageType()).isEqualTo(SMS);
						assertThat(historyEntry.getStatus()).isEqualTo(SENT);
						assertThat(historyEntry.getOrigin()).isEqualTo(ORIGIN);
						assertThat(historyEntry.getIssuer()).isEqualTo(ISSUER);
					});

				return true;
			});
	}

}

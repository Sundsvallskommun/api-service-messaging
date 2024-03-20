package apptest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.test.annotation.IntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;

@IntegrationTest
@WireMockAppTestSuite(files = "classpath:/MessagesIT/", classes = Application.class)
class MessagesIT extends AbstractMessagingAppTest {

	private static final String SERVICE_PATH = "/messages";

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private HistoryRepository historyRepository;

	@Test
	void test1_successfulRequest_bySms() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withHeader("x-origin", "Test-origin")
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull()
			.allSatisfy(historyEntry -> {
				assertThat(historyEntry.getBatchId()).as("batchId").isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).as("messageId").isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
			});
	}

	@Test
	void test2_successfulRequest_byEmail() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull()
			.allSatisfy(historyEntry -> {
				assertThat(historyEntry.getBatchId()).as("batchId").isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).as("messageId").isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
			});
	}

	@Test
	void test3_noContactSettingsFound() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull()
			.allSatisfy(historyEntry -> {
				assertThat(historyEntry.getBatchId()).as("batchId").isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).as("messageId").isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(NO_CONTACT_SETTINGS_FOUND);
			});
	}

	@Test
	void test4_noContactWanted() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);


		var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(1);
		var deliveryId = response.messages().getFirst().deliveries().getFirst().deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull().allSatisfy(historyEntry -> {
				assertThat(historyEntry.getBatchId()).as("batchId").isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).as("messageId").isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(NO_CONTACT_WANTED);
			});
	}

	@Test
	void test5_successfulRequest_byBothSmsAndEmail() throws Exception {
		var response = setupCall()
			.withServicePath(SERVICE_PATH)
			.withRequest("request.json")
			.withHttpMethod(POST)
			.withExpectedResponseStatus(CREATED)
			.withExpectedResponseHeader(LOCATION, List.of("^/status/batch/(.*)$"))
			.sendRequestAndVerifyResponse()
			.andReturnBody(MessageBatchResult.class);

		var batchId = response.batchId();

		assertThat(response.messages()).hasSize(1);
		var messageId = response.messages().getFirst().messageId();

		assertThat(response.messages().getFirst().deliveries()).hasSize(2);
		var deliveryId1 = response.messages().getFirst().deliveries().getFirst().deliveryId();
		var deliveryId2 = response.messages().getFirst().deliveries().get(1).deliveryId();

		// Make sure we received batch, message and delivery id:s as proper UUID:s
		assertValidUuid(batchId);
		assertValidUuid(messageId);
		assertValidUuid(deliveryId1);
		assertValidUuid(deliveryId2);

		// Make sure that there doesn't exist a message entity
		assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
		// Make sure that there exists a history entry with the correct id and status
		assertThat(historyRepository.findByMessageId(messageId))
			.isNotNull()
			.allSatisfy(historyEntry -> {
				assertThat(historyEntry.getBatchId()).as("batchId").isEqualTo(batchId);
				assertThat(historyEntry.getMessageId()).as("messageId").isEqualTo(messageId);
				assertThat(historyEntry.getStatus()).isEqualTo(SENT);
			});
	}
}

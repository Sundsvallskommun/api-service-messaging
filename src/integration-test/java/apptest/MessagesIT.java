package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_CONTACT_WANTED;
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
            .withRequest("request.json")
            .withHttpMethod(POST)
            .withExpectedResponseStatus(CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        var batchId = response.batchId();

        assertThat(response.messages()).hasSize(1);
        var messageId = response.messages().get(0).messageId();

        assertThat(response.messages().get(0).deliveries()).hasSize(1);
        var deliveryId = response.messages().get(0).deliveries().get(0).deliveryId();

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
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        var batchId = response.batchId();

        assertThat(response.messages()).hasSize(1);
        var messageId = response.messages().get(0).messageId();

        assertThat(response.messages().get(0).deliveries()).hasSize(1);
        var deliveryId = response.messages().get(0).deliveries().get(0).deliveryId();

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
    void test3_noFeedbackSettingsFound() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(POST)
            .withExpectedResponseStatus(CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        var batchId = response.batchId();

        assertThat(response.messages()).hasSize(1);
        var messageId = response.messages().get(0).messageId();

        assertThat(response.messages().get(0).deliveries()).hasSize(1);
        var deliveryId = response.messages().get(0).deliveries().get(0).deliveryId();

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
    void test4_noFeedbackWanted() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(POST)
            .withExpectedResponseStatus(CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);


        var batchId = response.batchId();

        assertThat(response.messages()).hasSize(1);
        var messageId = response.messages().get(0).messageId();

        assertThat(response.messages().get(0).deliveries()).hasSize(1);
        var deliveryId = response.messages().get(0).deliveries().get(0).deliveryId();

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
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        var batchId = response.batchId();

        assertThat(response.messages()).hasSize(1);
        var messageId = response.messages().get(0).messageId();

        assertThat(response.messages().get(0).deliveries()).hasSize(2);
        var deliveryId1 = response.messages().get(0).deliveries().get(0).deliveryId();
        var deliveryId2 = response.messages().get(0).deliveries().get(1).deliveryId();

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

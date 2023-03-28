package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.CREATED;
import static se.sundsvall.messaging.model.MessageStatus.NO_FEEDBACK_SETTINGS_FOUND;
import static se.sundsvall.messaging.model.MessageStatus.NO_FEEDBACK_WANTED;
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

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
        var deliveryId = response.messages().get(0).messageId();

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

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
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

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
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
                assertThat(historyEntry.getStatus()).isEqualTo(NO_FEEDBACK_SETTINGS_FOUND);
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

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
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
                assertThat(historyEntry.getStatus()).isEqualTo(NO_FEEDBACK_WANTED);
            });
    }
}

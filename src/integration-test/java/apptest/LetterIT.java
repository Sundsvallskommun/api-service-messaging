package apptest;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.FAILED;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.LETTER;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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

    private static final String SERVICE_PATH = "/letter";

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Test
    void test1_successfulRequestByDigital() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
        var deliveryId = response.messages().get(0).deliveryId();

        // Make sure we received batch, message and delivery id:s as proper UUID:s
        assertValidUuid(batchId);
        assertValidUuid(messageId);
        assertValidUuid(deliveryId);

        // Make sure that there doesn't exist a message entity
        assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

        var history = historyRepository.findByMessageId(messageId);
        // We should have two history entries
        assertThat(history).hasSize(2);
        // The batch id should be the same for everything in the history
        assertThat(history).extracting(HistoryEntity::getBatchId).containsOnly(batchId);
        // The message id should be the same for everything in the history
        assertThat(history).extracting(HistoryEntity::getMessageId).containsOnly(messageId);
        assertThat(history).extracting(HistoryEntity::getDeliveryId).contains(deliveryId);
        assertThat(history).extracting(HistoryEntity::getStatus)
            .containsExactlyInAnyOrder(SENT, SENT);
        assertThat(history).extracting(HistoryEntity::getMessageType)
            .containsExactlyInAnyOrder(LETTER, DIGITAL_MAIL);
    }

    @Test
    void test2_ErrorFromDigital_SuccessfulSnailMail() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        assertThat(response.messages()).hasSize(1);

        var batchId = response.batchId();
        var messageId = response.messages().get(0).messageId();
        var deliveryId = response.messages().get(0).deliveryId();

        // Make sure we received batch, message and delivery id:s as proper UUID:s
        assertValidUuid(batchId);
        assertValidUuid(messageId);
        assertValidUuid(deliveryId);

        // Make sure that there doesn't exist a message entity
        assertThat(messageRepository.existsByMessageId(messageId)).isFalse();

        var history = historyRepository.findByMessageId(messageId);
        // We should have three history entries
        assertThat(history).hasSize(3);
        // The batch id should be the same for everything in the history
        assertThat(history).extracting(HistoryEntity::getBatchId).containsOnly(batchId);
        // The message id should be the same for everything in the history
        assertThat(history).extracting(HistoryEntity::getMessageId).containsOnly(messageId);
        assertThat(history).extracting(HistoryEntity::getDeliveryId).contains(deliveryId);
        assertThat(history).extracting(HistoryEntity::getStatus)
            .containsExactlyInAnyOrder(SENT, FAILED, SENT);
        assertThat(history).extracting(HistoryEntity::getMessageType)
            .containsExactlyInAnyOrder(LETTER, DIGITAL_MAIL, SNAIL_MAIL);
    }

    @Test
    void test3_ErrorFromDigital_ErrorFromSnailmail() {
        setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY);
    }

    // TODO: create an additional app-test that tests the "ANY" delivery mode stuff...
}

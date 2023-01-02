package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.response.MessageBatchResult;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.model.MessageStatus;
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
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.CREATED)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageBatchResult.class);

        assertThat(response.messages()).hasSize(1);

        var messageId = response.messages().get(0).messageId();
        var batchId = response.batchId();

        // Make sure we received a message id and a batch id as proper UUID:s
        assertValidUuid(messageId);
        assertValidUuid(batchId);

        // Make sure that there doesn't exist a message entity
        assertThat(messageRepository.existsByMessageId(messageId)).isFalse();
        // Make sure that there exists a history entry with the correct id and status
        assertThat(historyRepository.findByMessageId(messageId))
            .isNotNull()
            .allSatisfy(historyEntry -> {
                assertThat(historyEntry.getMessageId()).isEqualTo(messageId);
                assertThat(historyEntry.getStatus()).isEqualTo(MessageStatus.SENT);
            });
    }

    @Test
    void test2_internalServerErrorFromDigitalMailSender() {
        setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.BAD_GATEWAY)
            .sendRequestAndVerifyResponse();
    }
}

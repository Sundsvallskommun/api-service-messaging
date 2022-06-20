package apptest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import se.sundsvall.dept44.common.validators.annotation.impl.ValidUuidConstraintValidator;
import se.sundsvall.dept44.test.annotation.wiremock.WireMockAppTestSuite;
import se.sundsvall.messaging.Application;
import se.sundsvall.messaging.api.model.MessageResponse;
import se.sundsvall.messaging.integration.db.HistoryRepository;
import se.sundsvall.messaging.integration.db.MessageRepository;
import se.sundsvall.messaging.model.MessageStatus;

@WireMockAppTestSuite(
    files = "classpath:/WebMessageIT/",
    classes = Application.class)
@Transactional
class WebMessageIT extends AbstractMessagingAppTest {

    private static final String SERVICE_PATH = "/webmessage";

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Override
    protected Optional<Duration> getVerificationDelay() {
        return Optional.of(Duration.ofSeconds(3));
    }

    @Test
    void test1_successfulRequest() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageResponse.class);

        // Make sure we received a message id as a proper UUID
        assertThat(response.getMessageId())
            .satisfies(messageId -> new ValidUuidConstraintValidator().isValid(messageId));

        // Make sure that there doesn't exist a message entity
        assertThat(messageRepository.existsById(response.getMessageId())).isFalse();
        // Make sure that there exists a history entry with the correct id and status
        assertThat(historyRepository.getReferenceById(response.getMessageId())).satisfies(historyEntry -> {
            assertThat(historyEntry.getMessageId()).isEqualTo(response.getMessageId());
            assertThat(historyEntry.getStatus()).isEqualTo(MessageStatus.SENT);
        });
    }

    @Test
    void test2_internalServerErrorFromWebMessageSender() throws Exception {
        var response = setupCall()
            .withServicePath(SERVICE_PATH)
            .withRequest("request.json")
            .withHttpMethod(HttpMethod.POST)
            .withExpectedResponseStatus(HttpStatus.OK)
            .sendRequestAndVerifyResponse()
            .andReturnBody(MessageResponse.class);

        // Make sure we received a message id as a proper UUID
        assertThat(response.getMessageId())
            .satisfies(messageId -> new ValidUuidConstraintValidator().isValid(messageId));

        // Make sure that there doesn't exist a message entity
        assertThat(messageRepository.existsById(response.getMessageId())).isFalse();
        // Make sure that there exists a history entry with the correct id and status
        assertThat(historyRepository.getReferenceById(response.getMessageId())).satisfies(historyEntry -> {
            assertThat(historyEntry.getMessageId()).isEqualTo(response.getMessageId());
            assertThat(historyEntry.getStatus()).isEqualTo(MessageStatus.FAILED);
        });
    }
}

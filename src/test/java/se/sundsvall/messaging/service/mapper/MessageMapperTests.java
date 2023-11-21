package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createValidDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidMessageRequestMessage;
import static se.sundsvall.messaging.TestDataFactory.createValidSlackRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidSnailMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createValidWebMessageRequest;
import static se.sundsvall.messaging.model.MessageStatus.PENDING;
import static se.sundsvall.messaging.model.MessageType.DIGITAL_MAIL;
import static se.sundsvall.messaging.model.MessageType.EMAIL;
import static se.sundsvall.messaging.model.MessageType.MESSAGE;
import static se.sundsvall.messaging.model.MessageType.SLACK;
import static se.sundsvall.messaging.model.MessageType.SMS;
import static se.sundsvall.messaging.model.MessageType.SNAIL_MAIL;
import static se.sundsvall.messaging.model.MessageType.WEB_MESSAGE;
import static se.sundsvall.messaging.util.JsonUtils.toJson;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
@ExtendWith(MockitoExtension.class)
class MessageMapperTests {

    private final MessageMapper messageMapper = new MessageMapper();

    @Test
    void test_toMessage_withEmailRequest() {
        var request = createValidEmailRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.type()).isEqualTo(EMAIL);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessage_withSnailMailRequest() {
        var request = createValidSnailMailRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.type()).isEqualTo(SNAIL_MAIL);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessage_withSmsRequest() {
        var request = createValidSmsRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.batchId()).isNull();
        assertThat(message.type()).isEqualTo(SMS);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessage_withWebMessageRequest() {
        var request = createValidWebMessageRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.batchId()).isNull();
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(WEB_MESSAGE);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessages_withDigitalMailRequest() {
        var request = createValidDigitalMailRequest();

        var messages = messageMapper.toMessages(request, "someBatchId");

        assertThat(messages).hasSize(1);

        var message = messages.get(0);

        assertThat(message.batchId()).isEqualTo("someBatchId");
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(DIGITAL_MAIL);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessage_withBatchIdAndMessageRequest() {
        var batchId = UUID.randomUUID().toString();

        var request = createValidMessageRequestMessage();

        var message = messageMapper.toMessage(batchId, request);

        assertThat(message.batchId()).isEqualTo(batchId);
        assertThat(message.messageId()).isNotNull();
        assertThat(message.type()).isEqualTo(MESSAGE);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }

    @Test
    void test_toMessage_withSlackRequest() {
        var request = createValidSlackRequest();

        var message = messageMapper.toMessage(request);

        assertThat(message.messageId()).isNotNull();
        assertThat(message.deliveryId()).isNotNull();
        assertThat(message.batchId()).isNull();
        assertThat(message.type()).isEqualTo(SLACK);
        assertThat(message.status()).isEqualTo(PENDING);
        assertThat(message.content()).isEqualTo(toJson(request));
    }
}

package se.sundsvall.messaging.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createDigitalMailRequest;
import static se.sundsvall.messaging.TestDataFactory.createEmailRequest;
import static se.sundsvall.messaging.TestDataFactory.createMessageRequest;
import static se.sundsvall.messaging.TestDataFactory.createSmsRequest;
import static se.sundsvall.messaging.TestDataFactory.createWebMessageRequest;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.integration.db.entity.MessageEntity;
import se.sundsvall.messaging.model.MessageStatus;
import se.sundsvall.messaging.model.MessageType;

class MessageMapperTests {

    private final MessageMapper messageMapper = new MessageMapper();

    @Test
    void test_toMessageDto() {
        var messageId = UUID.randomUUID().toString();
        var batchId = UUID.randomUUID().toString();

        var message = MessageEntity.builder()
            .withMessageId(messageId)
            .withBatchId(batchId)
            .build();

        var dto = messageMapper.toMessageDto(message);

        assertThat(dto.getMessageId()).isEqualTo(messageId);
        assertThat(dto.getBatchId()).isEqualTo(batchId);
    }

    @Test
    void test_toEntity_withEmailRequest() {
        var request = createEmailRequest();

        var message = messageMapper.toEntity(request);

        assertThat(message.getMessageId()).isNotNull();
        assertThat(message.getBatchId()).isNull();
        assertThat(message.getType()).isEqualTo(MessageType.EMAIL);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getContent()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toEntity_withSmsRequest() {
        var request = createSmsRequest();

        var message = messageMapper.toEntity(request);

        assertThat(message.getMessageId()).isNotNull();
        assertThat(message.getBatchId()).isNull();
        assertThat(message.getType()).isEqualTo(MessageType.SMS);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getContent()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toEntity_withWebMessageRequest() {
        var request = createWebMessageRequest();

        var message = messageMapper.toEntity(request);

        assertThat(message.getMessageId()).isNotNull();
        assertThat(message.getBatchId()).isNull();
        assertThat(message.getType()).isEqualTo(MessageType.WEB_MESSAGE);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getContent()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toEntity_withDigitalMailRequest() {
        var request = createDigitalMailRequest();

        var messages = messageMapper.toEntities(request, "someBatchId");

        assertThat(messages).hasSize(1);

        var message = messages.get(0);
        assertThat(message.getMessageId()).isNotNull();
        assertThat(message.getBatchId()).isEqualTo("someBatchId");
        assertThat(message.getType()).isEqualTo(MessageType.DIGITAL_MAIL);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getContent()).isEqualTo(MessageMapper.GSON.toJson(request));
    }

    @Test
    void test_toEntity_withBatchIdAndMessageRequest() {
        var batchId = UUID.randomUUID().toString();

        var request = createMessageRequest();

        var message = messageMapper.toEntity(batchId, request);

        assertThat(message.getMessageId()).isNotNull();
        assertThat(message.getBatchId()).isEqualTo(batchId);
        assertThat(message.getType()).isEqualTo(MessageType.MESSAGE);
        assertThat(message.getStatus()).isEqualTo(MessageStatus.PENDING);
        assertThat(message.getContent()).isEqualTo(MessageMapper.GSON.toJson(request));
    }
}

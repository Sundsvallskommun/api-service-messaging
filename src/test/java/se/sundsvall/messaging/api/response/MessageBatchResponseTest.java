package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class MessageBatchResponseTest {

    @Test
    void testBuilderAndGetters() {
        String batchId = UUID.randomUUID().toString();
        String messageId = UUID.randomUUID().toString();
        List<String> messageIds = List.of(messageId);

        MessageBatchResponse batchResponse = MessageBatchResponse.builder()
                .withBatchId(batchId)
                .withMessageIds(messageIds)
                .build();

        assertThat(batchResponse.getBatchId()).isEqualTo(batchId);
        assertThat(batchResponse.getMessageIds()).hasSize(1)
                .allSatisfy(id -> assertThat(id).isEqualTo(messageId));
    }
}

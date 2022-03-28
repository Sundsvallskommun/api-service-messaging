package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

public class MessageBatchResponseTest {

    @Test
    void testBuilderAndGetters() {
        var batchId = UUID.randomUUID().toString();
        var messageId = UUID.randomUUID().toString();
        var messageIds = List.of(messageId);

        var batchResponse = MessageBatchResponse.builder()
            .withBatchId(batchId)
            .withMessageIds(messageIds)
            .build();

        assertThat(batchResponse.getBatchId()).isEqualTo(batchId);
        assertThat(batchResponse.getMessageIds()).hasSize(1)
            .allSatisfy(id -> assertThat(id).isEqualTo(messageId));
    }
}

package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageBatchResultTests {

    @Test
    void testBuilderAndGetters() {
        var messageBatchResult = MessageBatchResult.builder()
            .withBatchId("someBatchId")
            .withMessages(List.of(
                MessageResult.builder().build()
            ))
            .build();

        assertThat(messageBatchResult.batchId()).isEqualTo("someBatchId");
        assertThat(messageBatchResult.messages()).hasSize(1);
    }
}

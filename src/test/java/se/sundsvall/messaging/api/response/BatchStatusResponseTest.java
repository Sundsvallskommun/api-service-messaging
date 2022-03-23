package se.sundsvall.messaging.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class BatchStatusResponseTest {

    @Test
    void testGetter() {
        List<MessageStatusResponse> statusResponses = List.of(MessageStatusResponse.builder().build());
        BatchStatusResponse batchStatusResponse = new BatchStatusResponse(statusResponses);

        assertThat(batchStatusResponse.getMessageStatuses()).hasSize(1);
    }
}

package se.sundsvall.messaging.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class BatchStatusResponseTest {

    @Test
    void testGetter() {
        var statusResponses = List.of(MessageStatusResponse.builder().build());
        var batchStatusResponse = new BatchStatusResponse(statusResponses);

        assertThat(batchStatusResponse.getMessageStatuses()).hasSize(1);
    }
}

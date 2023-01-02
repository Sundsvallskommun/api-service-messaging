package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class InternalDeliveryBatchResultTests {

    @Test
    void testDefaultConstructor() {
        var deliveryBatchResult = new InternalDeliveryBatchResult("someBatchId",
            List.of(new InternalDeliveryResult("someMessageId")));

        assertThat(deliveryBatchResult.batchId()).isEqualTo("someBatchId");
        assertThat(deliveryBatchResult.deliveries())
            .hasSize(1)
            .extracting(InternalDeliveryResult::messageId)
            .containsExactly("someMessageId");
    }
}

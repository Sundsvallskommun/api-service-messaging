package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;

import org.junit.jupiter.api.Test;

class ExternalReferenceTests {

    @Test
    void testBuilderAndGetters() {
        var externalReference = createExternalReference();

        assertThat(externalReference.getKey()).isEqualTo("someKey");
        assertThat(externalReference.getValue()).isEqualTo("someValue");
    }
}

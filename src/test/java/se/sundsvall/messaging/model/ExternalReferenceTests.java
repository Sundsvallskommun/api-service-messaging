package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createExternalReference;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class ExternalReferenceTests {

    @Test
    void testBuilderAndGetters() {
        var externalReference = createExternalReference();

        assertThat(externalReference.key()).isEqualTo("someKey");
        assertThat(externalReference.value()).isEqualTo("someValue");
    }
}

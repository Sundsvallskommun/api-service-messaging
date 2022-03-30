package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createHeader;

import org.junit.jupiter.api.Test;

class HeaderTests {

    @Test
    void testBuilderAndGetters() {
        var header = createHeader();

        assertThat(header.getName()).isEqualTo("someName");
        assertThat(header.getValues()).containsExactlyInAnyOrder("someValue1", "someValue2");
    }
}

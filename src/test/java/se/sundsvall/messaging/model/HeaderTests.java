package se.sundsvall.messaging.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static se.sundsvall.messaging.TestDataFactory.createHeader;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class HeaderTests {

    @Test
    void testBuilderAndGetters() {
        var header = createHeader();

        assertThat(header.name()).isEqualTo(HeaderName.CATEGORY);
        assertThat(header.values()).containsExactlyInAnyOrder("someValue1", "someValue2");
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class HeaderNameDeserializerTests {

        private final Header.HeaderNameDeserializer deserializer = new Header.HeaderNameDeserializer();

        @Mock
        private JsonParser mockJsonParser;
        @Mock
        private ObjectCodec mockObjectCodec;
        @Mock
        private JsonNode mockJsonNode;

        @BeforeEach
        void setUp() throws IOException {
            when(mockJsonParser.getCodec()).thenReturn(mockObjectCodec);
            when(mockObjectCodec.readTree(any(JsonParser.class))).thenReturn(mockJsonNode);
        }

        @Test
        void test_deserialize_ok() throws IOException {
            when(mockJsonNode.asText()).thenReturn(HeaderName.FACILITY_ID.name());

            var result = deserializer.deserialize(mockJsonParser, null);

            assertThat(result).isEqualTo(HeaderName.FACILITY_ID);
        }

        @Test
        void test_deserialize_invalid_value() throws IOException {
            when(mockJsonNode.asText()).thenReturn("someInvalidValue");

            var result = deserializer.deserialize(mockJsonParser, null);

            assertThat(result).isNull();
        }
    }
}

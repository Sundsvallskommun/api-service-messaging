package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.model.Header;
import se.sundsvall.messaging.test.annotation.UnitTest;

import generated.se.sundsvall.messagingrules.HeaderName;

@UnitTest
class SlackRequestTests {

    @Test
    void testConstructorAndGetters() {
        var headers = List.of(new Header(HeaderName.TYPE, List.of("someValue", "anotherValue")));
        var request = new SlackRequest(headers, "someToken", "someChannel", "someMessage");

        assertThat(request.headers()).hasSize(1).element(0).satisfies(header -> {
            assertThat(header.name()).isEqualTo(HeaderName.TYPE);
            assertThat(header.values()).containsExactlyInAnyOrder("someValue", "anotherValue");
        });
        assertThat(request.token()).isEqualTo("someToken");
        assertThat(request.channel()).isEqualTo("someChannel");
        assertThat(request.message()).isEqualTo("someMessage");
    }
}

package se.sundsvall.messaging.api.model.request;

import org.junit.jupiter.api.Test;
import se.sundsvall.messaging.test.annotation.UnitTest;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
class SlackRequestTests {

    @Test
    void testConstructorAndGetters() {
        var request = new SlackRequest("someToken", "someChannel", "someOrigin", "someMessage");

        assertThat(request.token()).isEqualTo("someToken");
        assertThat(request.channel()).isEqualTo("someChannel");
        assertThat(request.message()).isEqualTo("someMessage");
        assertThat(request.origin()).isEqualTo("someOrigin");
    }
}

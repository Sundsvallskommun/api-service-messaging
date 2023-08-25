package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SlackRequestTests {

    @Test
    void testConstructorAndGetters() {
        var request = new SlackRequest("someToken", "someChannel", "someMessage");

        assertThat(request.token()).isEqualTo("someToken");
        assertThat(request.channel()).isEqualTo("someChannel");
        assertThat(request.message()).isEqualTo("someMessage");
    }
}

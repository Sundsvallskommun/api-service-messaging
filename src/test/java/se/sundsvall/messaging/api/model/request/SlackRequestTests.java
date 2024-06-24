package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class SlackRequestTests {

	@Test
	void testConstructorAndGetters() {
		final var request = new SlackRequest("someToken", "someChannel", "someOrigin", "someMessage");

		assertThat(request).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(request.token()).isEqualTo("someToken");
		assertThat(request.channel()).isEqualTo("someChannel");
		assertThat(request.message()).isEqualTo("someMessage");
		assertThat(request.origin()).isEqualTo("someOrigin");
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(SlackRequest.builder().build()).hasAllNullFieldsOrProperties();
	}
}

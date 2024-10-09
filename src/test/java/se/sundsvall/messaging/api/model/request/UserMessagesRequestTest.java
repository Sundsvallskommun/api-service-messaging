package se.sundsvall.messaging.api.model.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class UserMessagesRequestTest {

	private static final String USER_ID = "userId";

	@Test
	void userMessagesRequestConstructor() {
		var userMessagesRequest = new UserMessagesRequest(USER_ID);

		assertThat(userMessagesRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userMessagesRequest.getUserId()).isEqualTo(USER_ID);
		assertThat(userMessagesRequest.getLimit()).isEqualTo(100);
		assertThat(userMessagesRequest.getPage()).isEqualTo(1);
	}

	@Test
	void userMessagesRequestBuilder() {
		var userMessagesRequest = UserMessagesRequest.builder()
			.withUserId(USER_ID)
			.build();

		assertThat(userMessagesRequest).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userMessagesRequest.getUserId()).isEqualTo(USER_ID);
		assertThat(userMessagesRequest.getLimit()).isEqualTo(100);
		assertThat(userMessagesRequest.getPage()).isEqualTo(1);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(UserMessagesRequest.builder().build()).hasAllNullFieldsOrPropertiesExcept("page", "limit");
	}
}

package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.TestDataFactory.createPagingMetaData;
import static se.sundsvall.messaging.TestDataFactory.createUserMessage;

import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;
import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class UserMessagesTest {

	private static final PagingMetaData PAGING_META_DATA = createPagingMetaData();
	private static final List<UserMessage> USER_MESSAGES = List.of(createUserMessage());

	@Test
	void userMessagesConstructor() {
		var userMessages = new UserMessages(PAGING_META_DATA, USER_MESSAGES);

		assertThat(userMessages).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userMessages.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(userMessages.messages()).isEqualTo(USER_MESSAGES);
		assertThat(userMessages).hasOnlyFields("metaData", "messages");
	}

	@Test
	void userMessagesBuilder() {
		var userMessages = UserMessages.builder()
			.withMetaData(PAGING_META_DATA)
			.withMessages(USER_MESSAGES)
			.build();

		assertThat(userMessages).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(userMessages.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(userMessages.messages()).isEqualTo(USER_MESSAGES);
		assertThat(userMessages).hasOnlyFields("metaData", "messages");
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(UserMessages.builder().build()).hasAllNullFieldsOrProperties();
	}
}

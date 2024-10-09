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
		var attachment = new UserMessages(PAGING_META_DATA, USER_MESSAGES);

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(attachment.messages()).isEqualTo(USER_MESSAGES);
	}

	@Test
	void userMessagesBuilder() {
		var attachment = UserMessages.builder()
			.withMetaData(PAGING_META_DATA)
			.withMessages(USER_MESSAGES)
			.build();

		assertThat(attachment).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(attachment.metaData()).isEqualTo(PAGING_META_DATA);
		assertThat(attachment.messages()).isEqualTo(USER_MESSAGES);
	}

	@Test
	void testNoDirtOnEmptyBean() {
		assertThat(Attachment.builder().build()).hasAllNullFieldsOrProperties();
	}
}

package se.sundsvall.messaging.api.model.response;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageBatchResultTest {

	@Test
	void testBuilderAndGetters() {
		final var messageBatchResult = MessageBatchResult.builder()
			.withBatchId("someBatchId")
			.withMessages(List.of(
				MessageResult.builder().build()))
			.build();

		assertThat(messageBatchResult).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(messageBatchResult.batchId()).isEqualTo("someBatchId");
		assertThat(messageBatchResult.messages()).hasSize(1);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(MessageBatchResult.builder().build()).hasAllNullFieldsOrProperties();
	}
}

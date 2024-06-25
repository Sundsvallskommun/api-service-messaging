package se.sundsvall.messaging.api.model.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class MessageResultTest {

	@Test
	void testBuilderAndGetters() {
		final var messageResult = MessageResult.builder()
			.withMessageId("someMessageId")
			.withDeliveries(List.of(DeliveryResult.builder().build()))
			.build();

		assertThat(messageResult).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(messageResult.messageId()).isEqualTo("someMessageId");
		assertThat(messageResult.deliveries()).isNotNull().hasSize(1);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(MessageResult.builder().build()).hasAllNullFieldsOrProperties();
	}
}

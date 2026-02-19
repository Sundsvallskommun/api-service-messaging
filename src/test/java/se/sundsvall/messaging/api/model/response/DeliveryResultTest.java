package se.sundsvall.messaging.api.model.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageStatus.SENT;
import static se.sundsvall.messaging.model.MessageType.SMS;

class DeliveryResultTest {

	@Test
	void testBuilderAndGetters() {
		final var deliveryResult = DeliveryResult.builder()
			.withDeliveryId("someDeliveryId")
			.withMessageType(SMS)
			.withStatus(SENT)
			.build();

		assertThat(deliveryResult).isNotNull().hasNoNullFieldsOrProperties();
		assertThat(deliveryResult.deliveryId()).isEqualTo("someDeliveryId");
		assertThat(deliveryResult.messageType()).isEqualTo(SMS);
		assertThat(deliveryResult.status()).isEqualTo(SENT);
	}

	@Test
	void testNoDirtOnCreatedBean() {
		assertThat(DeliveryResult.builder().build()).hasAllNullFieldsOrProperties();
	}
}

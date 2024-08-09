package se.sundsvall.messaging.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.messaging.model.MessageType.SMS;

import org.junit.jupiter.api.Test;

import se.sundsvall.messaging.test.annotation.UnitTest;

@UnitTest
class IncomingMessageEventTest {

	@Test
	void testConstructorAndGetter() {
		final var event = new IncomingMessageEvent("someSource", "2281", SMS, "someDeliveryId", "someOrigin");

		assertThat(event.getSource()).isEqualTo("someSource");
		assertThat(event.getMessageType()).isEqualTo(SMS);
		assertThat(event.getDeliveryId()).isEqualTo("someDeliveryId");
		assertThat(event.getOrigin()).isEqualTo("someOrigin");
	}

}
